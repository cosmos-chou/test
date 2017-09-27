package cn.rongcloud.im.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.jaeger.library.StatusBarUtil;

import cn.rongcloud.im.R;

/**
 * Created by cosmos on 17-9-27.
 */

public class ImmersiveActivity extends FragmentActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //沉浸式状态栏
        StatusBarUtil.setColor(this, extend.plugn.utils.UIUtils.getColor(R.color.de_title_bg), 0);
    }
}
