package extend.common;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.ui.activity.BaseActivity;
import extend.plugn.takephoto.VideoMessage;
import io.rong.imkit.RongIM;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created by cosmos on 17-8-22.
 */

public class VideoPlayerActivity extends BaseActivity implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, IRongCallback.IDownloadMediaMessageCallback {
    private static final int NOT_DOWNLOAD = 0;
    private static final int DOWNLOADED = 1;
    private static final int DOWNLOADING = 2;
    private static final int DELETED = 3;
    private static final int DOWNLOAD_ERROR = 4;
    private static final int DOWNLOAD_CANCEL = 5;
    private static final int DOWNLOAD_SUCCESS = 6;
    public static final String EXTRA_URI = "uri";
    public static final String EXTRA_VIDEO_MESSAGE = "EXTRA_VIDEO_MESSAGE";
    public static final String EXTRA_MESSAGE = "EXTRA_MESSAGE";

    public static final String TAG = "VideoPlayerActivity";
    private VideoView mVideoView;
    private Uri mUri;
    private int mPositionWhenPaused = -1;

    private MediaController mMediaController;

    private VideoMessage mVideoMessage;

    private Message mMessage;

    private FileDownloadInfo mFileDownloadInfo = new FileDownloadInfo();

    private  int mProgress;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        SealAppContext.getInstance().pushActivity(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.extend_activity_video_player);


        mVideoView = (VideoView) findViewById(R.id.video_view);

        mVideoMessage = getIntent().getParcelableExtra(EXTRA_VIDEO_MESSAGE);
        mMessage = getIntent().getParcelableExtra(EXTRA_MESSAGE);
        if(mVideoMessage != null){
            getFileDownloadInfo();
            beginOperation();
        }else{
            //Video file
            mUri = getIntent().getParcelableExtra(EXTRA_URI);
        }


        //Create media controller
        mMediaController = new MediaController(this);
        mVideoView.setMediaController(mMediaController);
    }

    public void onStart() {
        // Play Video
        if(mIsReady){
            beginPlay();
        }
        super.onStart();
    }

    public void onPause() {
        // Stop video when the activity is pause.
        stopPaly();
        super.onPause();
    }

    public void onResume() {
        // Resume video player
        if (mPositionWhenPaused >= 0) {
            mVideoView.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }

        super.onResume();
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
    }
    private void downloadFile() {
        String[] permission = new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"};
        if(!PermissionCheckUtil.checkPermissions(this, permission)) {
            PermissionCheckUtil.requestPermissions(this, permission);
        } else {
            this.mFileDownloadInfo.state = DOWNLOADING;
            RongIM.getInstance().downloadMediaMessage(this.mMessage, (IRongCallback.IDownloadMediaMessageCallback)null);
        }
    }

    @Override
    public void onSuccess(Message message) {
        mFileDownloadInfo.state = DOWNLOAD_SUCCESS;
        beginOperation();
    }

    @Override
    public void onProgress(Message message, int i) {
        mFileDownloadInfo.state = DOWNLOADING;
        mProgress = i;
        beginOperation();
    }

    @Override
    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
        mFileDownloadInfo.state = DOWNLOAD_ERROR;
        beginOperation();
    }

    @Override
    public void onCanceled(Message message) {
        mFileDownloadInfo.state = DOWNLOAD_CANCEL;
        beginOperation();
    }


    private void getFileDownloadInfo() {
        if(this.mVideoMessage.getLocalPath() != null) {
            String path = this.mVideoMessage.getLocalPath().getPath();
            if(path != null) {
                File file = new File(path);
                if(file.exists()) {
                    this.mFileDownloadInfo.state = DOWNLOADED;
                } else {
                    this.mFileDownloadInfo.state = DELETED;
                }
            }
        } /*else if(this.mProgress > 0 && this.mProgress < 100) {
            this.mFileDownloadInfo.state = DOWNLOADING;
            this.mFileDownloadInfo.progress = this.mProgress;
        } */else {
            this.mFileDownloadInfo.state = NOT_DOWNLOAD;
        }
    }

    public void beginOperation(){
        switch(this.mFileDownloadInfo.state) {
            case NOT_DOWNLOAD:
            case DELETED:
            case DOWNLOAD_ERROR:
            case DOWNLOAD_CANCEL:
                this.downloadFile();
                break;
            case DOWNLOADED:
            case DOWNLOAD_SUCCESS:
                mIsReady = true;
                beginPlay();;
                break;
            case DOWNLOADING:
                break;
        }

        System.out.println("mFileDownloadInfo.state " + mFileDownloadInfo.state + "  " + mProgress);
    }


    boolean mIsReady = false;
    public void beginPlay(){
        if(mIsReady){
            mVideoView.setVideoURI(mVideoMessage.getLocalPath());
            mVideoView.start();
        }
    }

    public void stopPaly(){
        if(mIsReady){
            mPositionWhenPaused = mVideoView.getCurrentPosition();
            mVideoView.stopPlayback();
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
