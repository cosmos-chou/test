package extend.plugn.takephoto;

import android.content.Intent;
import android.support.v4.app.Fragment;

import extend.plugn.utils.ReflectUtils;
import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.utilities.PermissionCheckUtil;

/**
 * Created by cosmos on 17-8-22.
 */

public class ImagePlugnEx extends ImagePlugin {

    public void onClick(Fragment currentFragment, RongExtension extension) {
        String[] permissions = new String[]{"android.permission.READ_EXTERNAL_STORAGE"};
        if (PermissionCheckUtil.requestPermissions(currentFragment, permissions)) {
            ReflectUtils.setValue(ImagePlugin.class, "conversationType", extension.getConversationType(), this);
            ReflectUtils.setValue(ImagePlugin.class, "targetId", extension.getTargetId(), this);
            Intent intent = new Intent(currentFragment.getActivity(), PictureSelectorActivityEx.class);
            extension.startActivityForPluginResult(intent, 23, this);
        }
    }
}