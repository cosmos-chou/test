package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.im.SealAppContext;
import cn.rongcloud.im.ui.activity.ReadReceiptDetailActivity;
import extend.plugn.takephoto.VideoMessage;
import extend.plugn.takephoto.VideoMessageHandler;
import extend.plugn.utils.VideoThumbLoader;
import io.rong.common.FileUtils;
import io.rong.imkit.RongContext;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.manager.SendImageManager;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.adapter.MessageListAdapter;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;

/**
 *  会话 Fragment 继承自ConversationFragment
 *  onResendItemClick: 重发按钮点击事件. 如果返回 false,走默认流程,如果返回 true,走自定义流程
 *  onReadReceiptStateClick: 已读回执详情的点击事件.
 *  如果不需要重写 onResendItemClick 和 onReadReceiptStateClick ,可以不必定义此类,直接集成 ConversationFragment 就可以了
 *  Created by Yuejunhong on 2016/10/10.
 */
public class ConversationFragmentEx extends ConversationFragment {
    public static final String PUSH_CONTENT_VIDEO = "视频";
    @Override
    public boolean onResendItemClick(io.rong.imlib.model.Message message) {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);
        if(view != null){
            view.setBackgroundColor(Color.WHITE);
        }

        return view;
    }

    @Override
    public void onReadReceiptStateClick(io.rong.imlib.model.Message message) {
        if (message.getConversationType() == Conversation.ConversationType.GROUP) { //目前只适配了群组会话
            Intent intent = new Intent(getActivity(), ReadReceiptDetailActivity.class);
            intent.putExtra("message", message);
            getActivity().startActivity(intent);
        }
    }

    public void onWarningDialog(String msg) {
        String typeStr = getUri().getLastPathSegment();
        if (!typeStr.equals("chatroom")) {
            super.onWarningDialog(msg);
        }
    }

    @Override
    public MessageListAdapter onResolveAdapter(Context context) {
        return super.onResolveAdapter(context);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void handleTakePhotoResult(Intent data){
        if (data != null) {
            String path = data.getStringExtra("path");
            if (data.getBooleanExtra("take_photo", true)) {
                List<Uri> images = new ArrayList<>();
                images.add(Uri.fromFile(new File(path)));
                SendImageManager.getInstance().sendImages(getConversationType(), getTargetId(), images, true);
            } else {
                //小视频
                sendVideoMsg(new File(path));
            }
        }
    }

    public void sendVideoMsg(final File file) {

        VideoThumbLoader.getInstance().showThumb(file.getAbsolutePath(), new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = VideoThumbLoader.getInstance().getVideoThumbToCache(file.getAbsolutePath());
                if(bitmap == null){
                    return;
                }
                ByteArrayOutputStream bos = null;
                File thumnail = null;
                try{
                    bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, VideoMessageHandler.THUMB_COMPRESSED_QUALITY, bos);
                    byte[] data = bos.toByteArray();
                    String path = SealAppContext.getInstance().getContext().getCacheDir().getAbsolutePath();
                    String name = "temp_" + System.currentTimeMillis() + ".jpg";
                    thumnail = FileUtils.byte2File(data, path, name);
                    if(!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    if(bos != null){
                        try {
                            bos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                VideoMessage videoMessage = VideoMessage.obtain(Uri.fromFile(file));
                if(thumnail != null){
                    videoMessage.setThumUri(Uri.fromFile(thumnail));
                }
                Message fileMessage = Message.obtain(getTargetId(), getConversationType(), videoMessage);
                RongIMClient.getInstance().sendMediaMessage(fileMessage, PUSH_CONTENT_VIDEO, "", new IRongCallback.ISendMediaMessageCallback() {
                    @Override
                    public void onProgress(Message message, int progress) {
                        //发送进度
                        message.setExtra(progress + "");
                        updateMessageStatus(message);

                        System.out.println("onProgress " + progress);
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    @Override
                    public void onCanceled(Message message) {
                        System.out.println("onCanceled " + message);
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    @Override
                    public void onAttached(Message message) {
                        //保存数据库成功
                        getMessageAdapter().add(UIMessage.obtain(message));
                        System.out.println("onAttached " + message);
                        onEventMainThread(message);
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    @Override
                    public void onSuccess(Message message) {
                        //发送成功
                        updateMessageStatus(message);
                        System.out.println("onSuccess " + message);
                        RongContext.getInstance().getEventBus().post(message);
                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                        //发送失败
                        updateMessageStatus(message);
                        System.out.println("onError " + message + "  " + errorCode );
                        RongContext.getInstance().getEventBus().post(message);
                    }
                });
            }
        }, 200, 200);


    }
    private void updateMessageStatus(Message message) {
        MessageListAdapter adapter = getMessageAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            Message msg = adapter.getItem(i).getMessage();
            if (msg.getMessageId() == message.getMessageId()) {
                adapter.remove(i);
                int value = -1;
                try{
                    value = Integer.valueOf(message.getExtra());
                }catch (Exception e){
                    e.printStackTrace();;
                }
                UIMessage um = UIMessage.obtain(message);
                um.setProgress(value);
                adapter.add(um);
                adapter.notifyDataSetChanged();
                break;
            }
        }

    }
}
