package extend.plugn.takephoto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.cjt2325.cameralibrary.JCameraView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.ui.activity.BaseActivity;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;


/**
 * @创建者 CSDN_LQR
 * @描述 拍照界面
 */
public class TakePhotoActivity extends BaseActivity {


    private JCameraView mJCameraView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SealAppContext.getInstance().pushActivity(this);
        init();
        setTitle(R.string.ex_plugn_title_take_photo);
        //子类不再需要设置布局ID，也不再需要使用ButterKnife.bind()
        setContentView(provideContentViewId());
        initView();
        initListener();
    }
    public void init() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            PermissionGen.with(this)
                    .addRequestCode(100)
                    .permissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                    .request();
    }

    public void initView() {
        mJCameraView = (JCameraView) findViewById(R.id.cameraview);
        //(0.0.7+)设置视频保存路径（如果不设置默认为Environment.getExternalStorageDirectory().getPath()）
        mJCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath());
        //(0.0.8+)设置手动/自动对焦，默认为自动对焦
        mJCameraView.setAutoFoucs(false);
        //设置小视频保存路径
        mJCameraView.setSaveVideoPath(getCacheDir().getAbsolutePath());
    }

    public void initListener() {
        mJCameraView.setCameraViewListener(new JCameraView.CameraViewListener() {
            @Override
            public void quit() {
                //返回按钮的点击时间监听
                finish();
            }

            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取到拍照成功后返回的Bitmap
                String path = saveBitmap(bitmap, getCacheDir().getAbsolutePath());
                Intent data = new Intent();
                data.putExtra("take_photo", true);
                data.putExtra("path", path);
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void recordSuccess(String url) {
                //获取成功录像后的视频路径
                Intent data = new Intent();
                data.putExtra("take_photo", false);
                data.putExtra("path", url);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @PermissionSuccess(requestCode = 100)
    public void permissionSuccess() {

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                recreate();
            }
        }, 500);
    }

    @PermissionFail(requestCode = 100)
    public void permissionFail() {
        Toast.makeText(this, "获取相机权限失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mJCameraView != null)
            mJCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mJCameraView != null)
            mJCameraView.onPause();
    }

    protected int provideContentViewId() {
        return R.layout.activity_take_photo;
    }

    public String saveBitmap(Bitmap bm, String dir) {
        String path = "";
        File f = new File(dir, "CSDN_LQR_" + SystemClock.currentThreadTimeMillis() + ".png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            path = f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SealAppContext.getInstance().popActivity(this);
    }
}
