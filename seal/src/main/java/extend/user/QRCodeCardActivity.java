package extend.user;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import cn.rongcloud.im.App;
import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.SealUserInfoManager;
import cn.rongcloud.im.ui.activity.BaseActivity;
import extend.common.GsonDao;
import extend.common.ScanActivity;
import extend.common.wangtiansoft.EncryDao;
import extend.plugn.utils.AppConst;
import extend.plugn.utils.LogUtils;
import extend.plugn.utils.UIUtils;
import io.rong.eventbus.EventBus;
import io.rong.imageloader.core.ImageLoader;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.UserInfo;

public class QRCodeCardActivity extends BaseActivity {

    private String mUserId;
    private String mGroupId;
    private String mGroupName;
    private String mGroupUrl;

    @Bind(R.id.ivHeader)
    ImageView mIvHeader;
    @Bind(R.id.ngiv)
    ImageView mNgiv;
    @Bind(R.id.tvName)
    TextView mTvName;
    @Bind(R.id.ivCard)
    ImageView mIvCard;
    @Bind(R.id.tvTip)
    TextView mTvTip;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SealAppContext.getInstance().popActivity(this);
        EventBus.getDefault().unregister(this);
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        SealAppContext.getInstance().pushActivity(this);
        setContentView(provideContentViewId());
        ButterKnife.bind(this);
        //沉浸式状态栏
        StatusBarUtil.setColor(this, extend.plugn.utils.UIUtils.getColor(R.color.colorPrimaryDark), 10);
        init();
        initView();
        initData();
    }

    public void init() {
        mGroupId = getIntent().getStringExtra("groupId");
        mGroupName = getIntent().getStringExtra("groupName");
        mGroupUrl = getIntent().getStringExtra("groupUrl");
    }

    public void initView() {
        mTvTip.setText(UIUtils.getString(R.string.qr_code_card_tip));
    }

    public void initData() {
        if (TextUtils.isEmpty(mGroupId)) {
            String userId = RongIM.getInstance().getCurrentUserId();
            if (!TextUtils.isEmpty(userId)) {
                SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
                String cacheName = sp.getString(SealConst.SEALTALK_LOGIN_NAME, "");
                String cachePortrait = sp.getString(SealConst.SEALTALK_LOGING_PORTRAIT, "");
                if (!TextUtils.isEmpty(cacheName)) {
                    mTvName.setText(cacheName);
                    String cacheId = sp.getString(SealConst.SEALTALK_LOGIN_ID, "a");
                    String portraitUri = SealUserInfoManager.getInstance().getPortraitUri(new UserInfo(
                            cacheId, cacheName, Uri.parse(cachePortrait)));
                    ImageLoader.getInstance().displayImage(portraitUri, mIvHeader, App.getOptions());
                }
                ScanActivity.ScanResult.FriendInfo friendInfo = new ScanActivity.ScanResult.FriendInfo();
                friendInfo.name = cacheName;
                friendInfo.userId = userId;
                setQRCode(AppConst.QrCodeCommon.ADD + EncryDao.encry(GsonDao.toJsonString(friendInfo), EncryDao.KEY));
                mTitle.setText(R.string.qr_code_card);
            }
        } else {
            mTvName.setText(mGroupName);
            mNgiv.setVisibility(View.VISIBLE);
            mIvHeader.setVisibility(View.GONE);
            ImageLoader.getInstance().displayImage(mGroupUrl, mNgiv, App.getOptions());
            ScanActivity.ScanResult.GroupInfo groupInfo = new ScanActivity.ScanResult.GroupInfo();
            groupInfo.groupId = mGroupId;
            setQRCode(AppConst.QrCodeCommon.JOIN + EncryDao.encry(GsonDao.toJsonString(groupInfo), EncryDao.KEY));
            mTvTip.setVisibility(View.GONE);
            mTitle.setText(R.string.group_qr_code);
        }
    }

    private void setQRCode(String content) {
        EventBus.getDefault().post(new QRCodeEvent.SyncEncodeQrcodeEvent(content));
    }

    private void loadQRCardError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
    }
    public void onEventBackgroundThread(QRCodeEvent.SyncEncodeQrcodeEvent event){
        if(event != null){
            EventBus.getDefault().post(new QRCodeEvent.BitmapEvent(QRCodeEncoder.syncEncodeQRCode(event.content, UIUtils.dip2Px(100))));
        }
    }

    public void onEventMainThread(QRCodeEvent.BitmapEvent event){
        if(event != null){
            mIvCard.setImageBitmap(event.bitmap);
        }
    }

    protected int provideContentViewId() {
        return R.layout.activity_qr_code_card;
    }

    public static class QRCodeEvent{
        public static class BitmapEvent{
            public Bitmap bitmap;

            public BitmapEvent(Bitmap bitmap) {
                this.bitmap = bitmap;
            }
        }

        public static class SyncEncodeQrcodeEvent{
            public String content;

            public SyncEncodeQrcodeEvent(String content) {
                this.content = content;
            }
        }
    }
}
