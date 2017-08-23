package extend.plugn.utils;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * @创建者 CSDN_LQR
 * @描述 视频缩略图加载工具
 */
public class VideoThumbLoader {

    static VideoThumbLoader instance;

    public static VideoThumbLoader getInstance() {
        if (instance == null) {
            synchronized (VideoThumbLoader.class) {
                if (instance == null) {
                    instance = new VideoThumbLoader();
                }
            }
        }
        return instance;
    }

    // 创建cache
    private LruCache<String, Bitmap> lruCache;
    private Map<String, Uri> thumnailCache = new HashMap<>();

    // @SuppressLint("NewApi")
    private VideoThumbLoader() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();// 获取最大的运行内存
        int maxSize = maxMemory / 8;
        lruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                // 这个方法会在每次存入缓存的时候调用
                // return value.getByteCount();
                return value.getRowBytes() * value.getHeight();
            }
        };
    }

    private void addVideoThumbToCache(String path, Bitmap bitmap) {
        if (getVideoThumbToCache(path) == null && bitmap != null) {
            // 当前地址没有缓存时，就添加
            lruCache.put(path, bitmap);
        }
    }

    public Bitmap getVideoThumbToCache(String path) {

        return lruCache.get(path);

    }

    public void showThumb(final String path, final ImageView imgview, int width, int height) {

        RunnableWrapper runnable = null;
        if(imgview != null){
            // 异步加载
            imgview.setTag(path);
            runnable = new RunnableWrapper() {
                @Override
                public void run() {
                    if (imgview != null && imgview.getTag() != null && imgview.getTag().equals(path)) {
                        imgview.setImageBitmap((Bitmap) paramsOut);
                    }
                }
            };
        }
    }

    public void showThumb(String path, Runnable runnable, int width, int height) {
        if (getVideoThumbToCache(path) == null) {
            new MyBobAsynctack(new RunnableWrapper(runnable), path, width, height).execute(path);
        } else {
            runnable.run();
        }

    }


    class MyBobAsynctack extends AsyncTask<String, Void, Bitmap> {
        private RunnableWrapper runnable;
        private String path;
        private int width;
        private int height;

        public MyBobAsynctack(RunnableWrapper runnable, String path, int width,
                              int height) {
            this.runnable = runnable;
            this.path = path;
            this.width = width;
            this.height = height;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = createVideoThumbnail(params[0], width, height,
                    MediaStore.Video.Thumbnails.MICRO_KIND);
            // 加入缓存中
            if (getVideoThumbToCache(params[0]) == null && bitmap != null) {
                addVideoThumbToCache(path, bitmap);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if(runnable != null){
                runnable.paramsOut = bitmap;
                runnable.run();
            }
        }
    }

    public static class RunnableWrapper implements Runnable{
        public Object paramsIn;
        public Object paramsOut;
        private Runnable mTarget;
        RunnableWrapper(){

        }
        RunnableWrapper(Runnable runnable){
            mTarget = runnable;
        }


        @Override
        public void run() {
            if(mTarget != null){
                mTarget.run();
            }
        }
    }

    private static Bitmap createVideoThumbnail(String vidioPath, int width,
                                               int height, int kind) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(vidioPath, kind);
        System.out.println("bitmap : " + bitmap.getHeight() + "   " + bitmap.getWidth());
        if(bitmap.getWidth() != 0 && bitmap.getHeight() != 0){
            width = height * bitmap.getWidth() / bitmap.getHeight();
        }

        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width , height,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }
}
