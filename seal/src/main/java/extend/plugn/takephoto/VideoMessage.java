package extend.plugn.takephoto;

import android.os.Parcel;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;

/**
 * Created by cosmos on 2017/8/21.
 */
@MessageTag(value = "app:custom", flag =  MessageTag.ISPERSISTED)
public class VideoMessage extends FileMessage {
    public VideoMessage(byte[] data) {
        super(data);
    }
    public VideoMessage(Parcel in) {
        super(in);
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