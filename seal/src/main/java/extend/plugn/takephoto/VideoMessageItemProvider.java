package extend.plugn.takephoto;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.ViewGroup;

import cn.rongcloud.im.R;
import cn.rongcloud.im.SealAppContext;
import extend.common.VideoPlayerActivity;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imkit.widget.provider.ImageMessageItemProvider;
import io.rong.message.ImageMessage;

/**
 * Created by cosmos on 2017/8/21.
 */
@ProviderTag(messageContent = VideoMessage.class)
public class VideoMessageItemProvider extends IContainerItemProvider.MessageProvider<VideoMessage>{

    ImageMessageItemProvider provider = new ImageMessageItemProvider();

    @Override
    public void bindView(View view, int i, VideoMessage videoMessage, UIMessage uiMessage) {
        provider.bindView(view, i, ImageMessage.obtain(videoMessage.getThumUri(), videoMessage.getLocalPath()), uiMessage);
        view.findViewById(R.id.rc_img_play).setVisibility(View.VISIBLE);
    }

    @Override
    public Spannable getContentSummary(VideoMessage videoMessage) {
        return new SpannableString(SealAppContext.getInstance().getContext().getString(R.string.ex_rc_message_content_video));
    }

    @Override
    public void onItemClick(View view, int i, VideoMessage videoMessage, UIMessage uiMessage) {
//        provider.onItemClick(view, i, videoMessage, uiMessage);
        Intent intent = new Intent(view.getContext(), VideoPlayerActivity.class);
        Uri uri = videoMessage.getLocalPath();
        intent.putExtra(VideoPlayerActivity.EXTRA_PROGRESS, uiMessage.getProgress());
        intent.putExtra(VideoPlayerActivity.EXTRA_VIDEO_MESSAGE, videoMessage);
        intent.putExtra(VideoPlayerActivity.EXTRA_MESSAGE, uiMessage.getMessage());
        view.getContext().startActivity(intent);
    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        return provider.newView(context, viewGroup);
    }
}
