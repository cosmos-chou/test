package extend.common;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.ui.activity.BaseActivity;
import extend.plugn.takephoto.VideoMessage;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.Event;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imlib.model.Message;

/**
 * Created by cosmos on 17-8-22.
 */

public class VideoPlayerActivity extends BaseActivity implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private static final int NOT_DOWNLOAD = 0;
    private static final int DOWNLOADED = 1;
    private static final int DOWNLOADING = 2;
    private static final int DELETED = 3;
    private static final int DOWNLOAD_ERROR = 4;
    private static final int DOWNLOAD_CANCEL = 5;
    private static final int DOWNLOAD_SUCCESS = 6;

    private static final int ON_SUCCESS_CALLBACK = 100;
    private static final int ON_PROGRESS_CALLBACK = 101;
    private static final int ON_CANCEL_CALLBACK = 102;
    private static final int ON_ERROR_CALLBACK = 103;

    public static final String EXTRA_PROGRESS = "EXTRA_PROGRESS";
    public static final String EXTRA_VIDEO_MESSAGE = "EXTRA_VIDEO_MESSAGE";
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    public static final String TAG = "VideoPlayerActivity";
    private SurfaceView mVideoView;
    private ProgressBar mProgressBar;
    private int mPositionWhenPaused = 0;

    private MediaPlayer mMediaPlayer;

    private VideoMessage mVideoMessage;

    private Message mMessage;

    private FileDownloadInfo mFileDownloadInfo = new FileDownloadInfo();

    private boolean mSurfaceCreated = false;

    private  int mProgress;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        SealAppContext.getInstance().pushActivity(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        mHeadLayout.setVisibility(View.GONE);
        RongContext.getInstance().getEventBus().register(this);
        setContentView(R.layout.extend_activity_video_player);
        mVideoView = (SurfaceView) findViewById(R.id.video_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mVideoMessage = getIntent().getParcelableExtra(EXTRA_VIDEO_MESSAGE);
        mMessage = getIntent().getParcelableExtra(EXTRA_MESSAGE);
        mProgress = getIntent().getIntExtra(EXTRA_PROGRESS, 0);
        if(mVideoMessage != null){
            getFileDownloadInfo();
            beginOperation();
        }
        mVideoView.getHolder().setKeepScreenOn(true);
        mVideoView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceCreated = true;
                if(mIsReady){
                    beginPlay(mPositionWhenPaused);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mSurfaceCreated = false;
                stopPaly();
            }
        });
    }

    public void onStart() {
        // Play Video
        if(mIsReady && mSurfaceCreated){
            beginPlay(mPositionWhenPaused);
        }
        super.onStart();
    }

    public void onStop() {
        // Stop video when the activity is pause.
        stopPaly();
        super.onStop();
    }

    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        return false;
    }

    public void onCompletion(MediaPlayer mp) {
        this.finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SealAppContext.getInstance().popActivity(this);
        RongContext.getInstance().getEventBus().unregister(this);
    }
    private void downloadFile() {
        String[] permission = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
        if(!PermissionCheckUtil.checkPermissions(this, permission)) {
            PermissionCheckUtil.requestPermissions(this, permission);
        } else {
            this.mFileDownloadInfo.state = DOWNLOADING;
            RongIM.getInstance().downloadMediaMessage(this.mMessage, null);
        }
    }

    public void onEventMainThread(Event.FileMessageEvent event) {
        if(this.mMessage.getMessageId() == event.getMessage().getMessageId()) {
            switch(event.getCallBackType()) {
                case ON_SUCCESS_CALLBACK:
                    if(this.mFileDownloadInfo.state != DOWNLOAD_CANCEL) {
                        if(event.getMessage() == null || event.getMessage().getContent() == null) {
                            return;
                        }
                        VideoMessage fileMessage = (VideoMessage)event.getMessage().getContent();
                        this.mVideoMessage.setLocalPath(Uri.parse(fileMessage.getLocalPath().toString()));
                        this.mFileDownloadInfo.state = DOWNLOAD_SUCCESS;
                        this.mFileDownloadInfo.path = fileMessage.getLocalPath().toString();
                        beginOperation();
                    }
                    break;
                case ON_PROGRESS_CALLBACK:
                    if(this.mFileDownloadInfo.state != DOWNLOAD_CANCEL) {
                        this.mFileDownloadInfo.state = DOWNLOADING;
                        mProgress = this.mFileDownloadInfo.progress = event.getProgress();
                        beginOperation();
                    }
                    break;
                case ON_ERROR_CALLBACK:
                    if(this.mFileDownloadInfo.state != DOWNLOAD_CANCEL) {
                        this.mFileDownloadInfo.state = DOWNLOAD_ERROR;
                        beginOperation();
                    }
                    break;
                case ON_CANCEL_CALLBACK:
                default:
                    break;
            }
        }

    }

    private void getFileDownloadInfo() {
        if(this.mVideoMessage.getLocalPath() != null) {
            String path = this.mVideoMessage.getLocalPath().getPath();
            System.out.println("getFileDownloadInfo  " + path);
            if(path != null) {
                File file = new File(path);
                if(file.exists()) {
                    this.mFileDownloadInfo.state = DOWNLOADED;
                } else {
                    this.mFileDownloadInfo.state = DELETED;
                }
            }
        } else {
            this.mFileDownloadInfo.state = NOT_DOWNLOAD;
        }
    }

    public void beginOperation(){
        switch(this.mFileDownloadInfo.state) {
            case NOT_DOWNLOAD:
            case DELETED:
            case DOWNLOAD_ERROR:
                this.downloadFile();
                break;
            case DOWNLOAD_CANCEL:
                mProgressBar.setVisibility(VideoView.GONE);
                break;
            case DOWNLOADED:
            case DOWNLOAD_SUCCESS:
                mIsReady = true;
                mProgressBar.setVisibility(VideoView.GONE);
                if(mSurfaceCreated){
                    beginPlay(mPositionWhenPaused);
                }
                break;
            case DOWNLOADING:
                mProgressBar.setProgress(mProgress);
                mProgressBar.setVisibility(VideoView.VISIBLE);
                break;
        }
    }


    boolean mIsReady = false;
    public void beginPlay(final int mesc){
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
            return;
        }

        if(mIsReady){
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mMediaPlayer.setDataSource(this, mVideoMessage.getLocalPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.prepareAsync();

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    try{
                        mMediaPlayer.setDisplay(mVideoView.getHolder());
                        mMediaPlayer.start();
                        mMediaPlayer.seekTo(mesc);
                    }catch (Exception ee){
                        ee.printStackTrace();
                        if(mMediaPlayer != null){
                            mMediaPlayer.reset();
                            mMediaPlayer.release();
                            mMediaPlayer = null;
                        }
                    }
                }
            });
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnErrorListener(this);
        }
    }

    public void stopPaly(){
        if (mIsReady && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mPositionWhenPaused = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    class FileDownloadInfo {
        int state;
        int progress;
        String path;

        FileDownloadInfo() {
        }
    }

}
