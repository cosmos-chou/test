package cn.rongcloud.im.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

import cn.rongcloud.im.R;
import cn.rongcloud.im.ui.activity.ReadReceiptDetailActivity;
import extend.plugn.takephoto.VideoMessage;
import extend.plugn.utils.ImageUtils;
import extend.plugn.utils.VideoThumbLoader;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AutoRefreshListView;
import io.rong.imkit.widget.adapter.MessageListAdapter;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.ImageMessage;

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
                //照片
                sendImgMsg(ImageUtils.genThumbImgFile(path), new File(path));
            } else {
                //小视频
                sendVideoMsg(new File(path));
            }
        }
    }


    public void sendImgMsg(File imageFileThumb, File imageFileSource) {
        Uri imageFileThumbUri = Uri.fromFile(imageFileThumb);
        Uri imageFileSourceUri = Uri.fromFile(imageFileSource);
        sendImgMsg(imageFileThumbUri, imageFileSourceUri);
    }

    public void sendVideoMsg(final File file) {

        VideoThumbLoader.getInstance().showThumb(file.getAbsolutePath(), new Runnable() {
            @Override
            public void run() {
                Message fileMessage = Message.obtain(getTargetId(), getConversationType(), VideoMessage.obtain(Uri.fromFile(file)));
                RongIMClient.getInstance().sendMediaMessage(fileMessage, PUSH_CONTENT_VIDEO, "", new IRongCallback.ISendMediaMessageCallback() {
                    @Override
                    public void onProgress(Message message, int progress) {
                        //发送进度
                        message.setExtra(progress + "");
                        updateMessageStatus(message);

                        System.out.println("onProgress " + progress);
                    }

                    @Override
                    public void onCanceled(Message message) {
                        System.out.println("onCanceled " + message);
                    }

                    @Override
                    public void onAttached(Message message) {
                        //保存数据库成功
                        getMessageAdapter().add(UIMessage.obtain(message));
                        System.out.println("onAttached " + message);
                        onEventMainThread(message);
                    }

                    @Override
                    public void onSuccess(Message message) {
                        //发送成功
                        updateMessageStatus(message);
                        System.out.println("onSuccess " + message);
                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                        //发送失败
                        updateMessageStatus(message);
                        System.out.println("onError " + message + "  " + errorCode );
                    }
                });
            }
        }, 200, 200);


    }

    private void moveToBottom(){
        if(getView() != null){
            AutoRefreshListView listView = findViewById(getView(), R.id.rc_list);
            if(listView != null){
                listView.smoothScrollToPosition(getMessageAdapter().getCount() - 1);
            }
        }
    }

    public void sendImgMsg(Uri imageFileThumbUri, Uri imageFileSourceUri) {
        ImageMessage imgMsg = ImageMessage.obtain(imageFileThumbUri, imageFileSourceUri);
        RongIMClient.getInstance().sendImageMessage(getConversationType(), getTargetId(), imgMsg, PUSH_CONTENT_VIDEO, "",
                new RongIMClient.SendImageMessageCallback() {
                    @Override
                    public void onAttached(Message message) {
                        //保存数据库成功
                        getMessageAdapter().add(UIMessage.obtain(message));
                        onEventMainThread(message);
                    }

                    @Override
                    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                        //发送失败
                        updateMessageStatus(message);
                    }

                    @Override
                    public void onSuccess(Message message) {
                        //发送成功
                        updateMessageStatus(message);
                    }

                    @Override
                    public void onProgress(Message message, int progress) {
                        //发送进度
                        message.setExtra(progress + "");
                        updateMessageStatus(message);
                    }
                });
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
