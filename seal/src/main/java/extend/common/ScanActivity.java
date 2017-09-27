package extend.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.db.Friend;
import cn.rongcloud.im.ui.activity.BaseActivity;
import cn.rongcloud.im.ui.activity.UserDetailActivity;
import extend.common.thread.ThreadPoolFactory;
import extend.common.wangtiansoft.EncryDao;
import extend.engine.JoinGroupEngine;
import extend.plugn.utils.LogUtils;
import extend.plugn.utils.PopupWindowUtils;
import extend.plugn.utils.UIUtils;
import io.rong.common.RLog;
import io.rong.imkit.plugin.image.PictureSelectorActivity;

/**
 * @创建者 CSDN_LQR
 * @描述 扫一扫界面
 */
public class ScanActivity extends BaseActivity implements QRCodeView.Delegate {
    private static final String TAG = "ScanActivity";
    public static final int IMAGE_PICKER = 100;
    @Bind(R.id.zxingview)
    ZXingView mZxingview;

    private FrameLayout mView;
    private PopupWindow mPopupWindow;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SealAppContext.getInstance().pushActivity(this);

        setContentView(provideContentViewId());
        ButterKnife.bind(this);

        initView();
        initListener();
    }

    public void initView() {
        mBtnRight.setBackgroundResource(R.drawable.ic_friend_more);
        mBtnRight.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.qr_cord_or_bar_code);
    }

    public void initListener() {
        mBtnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });
        mZxingview.setDelegate(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mZxingview.startCamera();
        mZxingview.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mZxingview.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mZxingview.onDestroy();
        SealAppContext.getInstance().popActivity(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {//返回多张照片
            if (data != null) {
                final ArrayList<Uri> images =  data.getParcelableArrayListExtra("android.intent.extra.RETURN_RESULT");
                if (images != null && images.size() > 0 && images.get(0) != null) {
                    //取第一张照片
                    ThreadPoolFactory.getNormalPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            String result = QRCodeDecoder.syncDecodeQRCode(new File(URI.create(images.get(0).toString())).getAbsolutePath());
                            if (TextUtils.isEmpty(result)) {
                                UIUtils.showToast(UIUtils.getString(R.string.scan_fail));
                            } else {
                                handleResult(result);
                            }
                        }
                    });
                }
            }
        }
    }


    protected int provideContentViewId() {
        return R.layout.activity_scan;
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        handleResult(result);
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        UIUtils.showToast(UIUtils.getString(R.string.open_camera_error));
    }


    private void showPopupMenu() {
        if (mView == null) {
            mView = new FrameLayout(this);
            mView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mView.setBackgroundColor(UIUtils.getColor(R.color.white));

            TextView tv = new TextView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, UIUtils.dip2Px(45));
            tv.setLayoutParams(params);
            tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            tv.setPadding(UIUtils.dip2Px(20), 0, 0, 0);
            tv.setTextColor(UIUtils.getColor(R.color.gray0));
            tv.setTextSize(14);
            tv.setText(UIUtils.getString(R.string.select_qr_code_from_ablum));
            mView.addView(tv);

            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupWindow.dismiss();
                    Intent intent = new Intent(ScanActivity.this, PictureSelectorActivity.class);
                    startActivityForResult(intent, IMAGE_PICKER);
                }
            });
        }
        mPopupWindow = PopupWindowUtils.getPopupWindowAtLocation(mView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, getWindow().getDecorView().getRootView(), Gravity.BOTTOM, 0, 0);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                PopupWindowUtils.makeWindowLight(ScanActivity.this);
            }
        });
        PopupWindowUtils.makeWindowDark(this);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    private void handleResult(String result) {
        RLog.i(TAG, "扫描结果:" + result);
        vibrate();
        mZxingview.startSpot();
        //添加好友
        if (result.startsWith(AppConst.QrCodeCommon.ADD)) {
            result = result.substring(AppConst.QrCodeCommon.ADD.length());
            ScanResult.FriendInfo info = GsonDao.fromJson(EncryDao.deEncry(result, EncryDao.KEY), ScanResult.FriendInfo.class);

            if(info != null && !TextUtils.isEmpty(info.userId)){
                if (SealUserInfoManager.getInstance().isFriendsRelationship(info.userId)) {
                    UIUtils.showToast(UIUtils.getString(R.string.this_account_was_your_friend));
                    return;
                }
                Intent intent = new Intent(ScanActivity.this, UserDetailActivity.class);
                intent.putExtra("friend", new Friend(info.userId, info.name, Uri.EMPTY));
                startActivity(intent);
                finish();
            }
        }
        //进群
        else if (result.startsWith(AppConst.QrCodeCommon.JOIN)) {
            result = result.substring(AppConst.QrCodeCommon.JOIN.length());
            ScanResult.GroupInfo info = GsonDao.fromJson(EncryDao.deEncry(result, EncryDao.KEY), ScanResult.GroupInfo.class);

            if(info != null && !TextUtils.isEmpty(info.groupId)) {

                if (SealUserInfoManager.getInstance().isInGoup(info.groupId)) {
                    UIUtils.showToast(UIUtils.getString(R.string.you_already_in_this_group));
                    return;
                } else {
                    new JoinGroupEngine(this).startEngine(info.groupId, new JoinGroupEngine.IGroupMembersCallback() {
                        @Override
                        public void onResult() {
                            finish();
                        }
                    });
                }
            }
        }
    }

    private void loadError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        UIUtils.showToast(throwable.getLocalizedMessage());
    }


    public static class ScanResult{

        public static class FriendInfo{
            public String userId;
            public String name;
        }



        public static class GroupInfo{
            public String groupId;
        }
    }

}
