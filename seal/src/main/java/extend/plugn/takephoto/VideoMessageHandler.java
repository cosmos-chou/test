package extend.plugn.takephoto;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import io.rong.common.FileUtils;
import io.rong.common.RLog;
import io.rong.imlib.NativeClient;
import io.rong.imlib.model.Message;
import io.rong.message.MessageHandler;
import io.rong.message.utils.BitmapUtil;

/**
 * Created by cosmos on 17-8-23.
 */

public class VideoMessageHandler extends MessageHandler<VideoMessage> {
    private static final String TAG = "VideoMessageHandler";
    public static int COMPRESSED_SIZE = 960;
    public static int COMPRESSED_QUALITY = 85;
    public static int THUMB_COMPRESSED_SIZE = 240;
    public static int THUMB_COMPRESSED_MIN_SIZE = 100;
    public static int THUMB_COMPRESSED_QUALITY = 30;
    public static final String IMAGE_THUMBNAIL_PATH = "/video/thumbnail/";
    public VideoMessageHandler(Context context) {
        super(context);
    }
    public void decodeMessage(Message message, VideoMessage model) {
        Uri uri = obtainImageUri(this.getContext());
        String name = message.getMessageId() + ".jpg";
        if(message.getMessageId() == 0) {
            name = message.getSentTime() + ".jpg";
        }
        String thumb = uri.toString() + IMAGE_THUMBNAIL_PATH;
        File thumbFile = new File(thumb + name);
        if(!TextUtils.isEmpty(model.getBase64()) && !thumbFile.exists()) {
            byte[] data = null;
            try {
                data = Base64.decode(model.getBase64(), 2);
            } catch (IllegalArgumentException var11) {
                RLog.e(TAG, "afterDecodeMessage Not Base64 Content!");
                var11.printStackTrace();
            }
            if(!isImageFile(data)) {
                RLog.e(TAG, "afterDecodeMessage Not Image File!");
                return;
            }
            FileUtils.byte2File(data, thumb, name);
        }

        model.setThumUri(Uri.parse("file://" + thumb + name));
        model.setBase64((String)null);
    }

    public void encodeMessage(Message message) {
        VideoMessage model = (VideoMessage)message.getContent();
        Uri uri = obtainImageUri(this.getContext());
        String name = message.getMessageId() + ".jpg";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Resources resources = this.getContext().getResources();

        try {
            COMPRESSED_QUALITY = resources.getInteger(resources.getIdentifier("rc_image_quality", "integer", this.getContext().getPackageName()));
            COMPRESSED_SIZE = resources.getInteger(resources.getIdentifier("rc_image_size", "integer", this.getContext().getPackageName()));
        } catch (Resources.NotFoundException var12) {
            var12.printStackTrace();
        }

        if(model.getThumUri() != null && model.getThumUri().getScheme() != null && model.getThumUri().getScheme().equals("file")) {
            File e = new File(uri.toString() + IMAGE_THUMBNAIL_PATH + name);
            byte[] file;
            if(e.exists()) {
                model.setThumUri(Uri.parse("file://" + uri.toString() + IMAGE_THUMBNAIL_PATH + name));
                file = FileUtils.file2byte(e);
                if(file != null) {
                    model.setBase64(Base64.encodeToString(file, 2));
                }
            } else {
                try {
                    String bitmap = model.getThumUri().toString().substring(5);
                    RLog.d(TAG, "beforeEncodeMessage Thumbnail not save yet! " + bitmap);
                    BitmapFactory.decodeFile(bitmap, options);
                    if(options.outWidth <= THUMB_COMPRESSED_SIZE && options.outHeight <= THUMB_COMPRESSED_SIZE) {
                        File dir1 = new File(bitmap);
                        file = FileUtils.file2byte(dir1);
                        if(file != null) {
                            model.setBase64(Base64.encodeToString(file, 2));
                            String bos1 = uri.toString() + IMAGE_THUMBNAIL_PATH;
                            if(FileUtils.copyFile(dir1, bos1, name) != null) {
                                model.setThumUri(Uri.parse("file://" + bos1 + name));
                            }
                        }
                    } else {
                        Bitmap dir = BitmapUtil.getThumbBitmap(this.getContext(), model.getThumUri(), THUMB_COMPRESSED_SIZE, THUMB_COMPRESSED_MIN_SIZE);
                        if(dir != null) {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            dir.compress(Bitmap.CompressFormat.JPEG, THUMB_COMPRESSED_QUALITY, bos);
                            file = bos.toByteArray();
                            model.setBase64(Base64.encodeToString(file, 2));
                            bos.close();
                            FileUtils.byte2File(file, uri.toString() + IMAGE_THUMBNAIL_PATH, name);
                            model.setThumUri(Uri.parse("file://" + uri.toString() + IMAGE_THUMBNAIL_PATH + name));
                            if(!dir.isRecycled()) {
                                dir.recycle();
                            }
                        }
                    }
                } catch (IllegalArgumentException var14) {
                    var14.printStackTrace();
                    RLog.e(TAG, "beforeEncodeMessage Not Base64 Content!");
                } catch (IOException var15) {
                    var15.printStackTrace();
                    RLog.e(TAG, "beforeEncodeMessage IOException");
                }
            }
        }

    }

    public static Uri obtainImageUri(Context context) {
        File file = context.getFilesDir();
        String path = file.getAbsolutePath();
        String userId = NativeClient.getInstance().getCurrentUserId();
        return Uri.parse(path + File.separator + userId);
    }


    public static String obtainImagePath(Context context) {
        File file = context.getFilesDir();
        String path = file.getAbsolutePath();
        String userId = NativeClient.getInstance().getCurrentUserId();
        return path + File.separator + userId + "/";
    }

    private static boolean isImageFile(byte[] data) {
        if(data != null && data.length != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            return options.outWidth != -1;
        } else {
            return false;
        }
    }
}
