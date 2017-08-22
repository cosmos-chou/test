package extend.plugn.takephoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.fragment.ConversationFragmentEx;
import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

/**
 * Created by cosmos on 17-8-21.
 */

public class TakePhotoPlugn implements IPluginModule {
    public static final int PLUGN_REQUEST_CODE_TAKE_PHOTO = 200;
    ConversationFragmentEx mFragment;

    @Override
    public Drawable obtainDrawable(Context context) {
        final Drawable inner = context.getResources().getDrawable(R.drawable.extend_rc_drawable_takephoto_selector);
        return inner;
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.ex_plugn_title_take_photo);
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        rongExtension.startActivityForPluginResult(new Intent(fragment.getContext(), TakePhotoActivity.class), PLUGN_REQUEST_CODE_TAKE_PHOTO, this);
        if (fragment instanceof ConversationFragmentEx) {
            mFragment = (ConversationFragmentEx) fragment;
        }
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {
        switch (i) {
            case TakePhotoPlugn.PLUGN_REQUEST_CODE_TAKE_PHOTO:
                switch (i1) {
                    case Activity.RESULT_OK:
                        if(mFragment != null){
                            mFragment.handleTakePhotoResult(intent);
                        }
                        break;
                }
                break;
        }
    }
}
