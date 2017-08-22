package extend.plugn.takephoto;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.rong.imlib.MessageTag;
import io.rong.message.FileMessage;

/**
 * Created by cosmos on 2017/8/21.
 */
@MessageTag(value = "app:video", flag =  MessageTag.ISPERSISTED | MessageTag.ISCOUNTED)
public class VideoMessage extends FileMessage {


    public VideoMessage(byte[] data) {
        super(data);
    }


    private String convertToBase64(Bitmap b){
        if(b != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 30, bos);
            String result = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        return null;
    }





    public Bitmap getBitmap(){
        return null;
    }

    public VideoMessage(Parcel in) {
        super(in);
    }

    public static VideoMessage obtain(Uri localUrl) {
        FileMessage fm = FileMessage.obtain(localUrl);
        if(fm != null){
            return new VideoMessage(fm.encode());
        }
        return null;
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<VideoMessage> CREATOR = new Creator<VideoMessage>() {

        @Override
        public VideoMessage createFromParcel(Parcel source) {
            return new VideoMessage(source);
        }

        @Override
        public VideoMessage[] newArray(int size) {
            return new VideoMessage[size];
        }
    };

    /**
     * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
     *
     * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
     */
    public int describeContents() {
        return 0;
    }

    /**
     * 将类的数据写入外部提供的 Parcel 中。
     *
     * @param dest  对象被写入的 Parcel。
     * @param flags 对象如何被写入的附加标志。
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
//        ParcelUtils.writeToParcel(dest, content);//该类为工具类，对消息中属性进行序列化
        //这里可继续增加你消息的属性
    }
}
