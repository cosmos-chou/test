package extend.plugn.takephoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.Fragment;

import cn.rongcloud.im.R;
import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

/**
 * Created by cosmos on 17-8-21.
 */

public class TakePhotoPlugn implements IPluginModule {
    public static final int PLUGN_REQUEST_CODE_TAKE_PHOTO = 10000;

    @Override
    public Drawable obtainDrawable(Context context) {
        final Drawable inner = context.getResources().getDrawable(R.drawable.ic_func_shot);
        final int size = context.getResources().getDimensionPixelSize(R.dimen.ex_plugn_image_size);
        GradientDrawable d = new GradientDrawable(){
            @Override
            public void draw(Canvas canvas) {
                super.draw(canvas);
                canvas.save();
                canvas.clipRect(size / 4, size / 4, 3 * size / 4, 3 * size / 4);
                inner.setBounds(0, 0, size / 2, size / 2);
                inner.draw(canvas);
                canvas.restore();;
            }
        };
        d.setBounds(0,0, size ,size);
        d.setCornerRadius(5);
        return d;
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.ex_plugn_title_take_photo);
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        fragment.startActivityForResult(new Intent(fragment.getContext(), TakePhotoActivity.class), PLUGN_REQUEST_CODE_TAKE_PHOTO);
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {
        switch (i){
            case PLUGN_REQUEST_CODE_TAKE_PHOTO:
                switch (i1){
                    case Activity.RESULT_OK:
                        if(intent != null){
                            String path = intent.getStringExtra("path");
                            if (intent.getBooleanExtra("take_photo", true)) {
                                //照片
//                                mPresenter.sendImgMsg(ImageUtils.genThumbImgFile(path), new File(path));
                            } else {
                                //小视频
//                                mPresenter.sendFileMsg(new File(path));
                            }
                        }
                        break;
                }
                break;
        }
    }
}
