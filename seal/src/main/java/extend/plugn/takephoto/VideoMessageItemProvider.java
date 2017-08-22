package extend.plugn.takephoto;

import android.content.Context;
import android.text.Spannable;
import android.view.View;
import android.view.ViewGroup;

import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.provider.IContainerItemProvider;

/**
 * Created by cosmos on 2017/8/21.
 */

public class VideoMessageItemProvider extends IContainerItemProvider.MessageProvider<VideoMessage>{

    @Override
    public void bindView(View view, int i, VideoMessage videoMessage, UIMessage uiMessage) {

    }

    @Override
    public Spannable getContentSummary(VideoMessage videoMessage) {
        return null;
    }

    @Override
    public void onItemClick(View view, int i, VideoMessage videoMessage, UIMessage uiMessage) {

    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        return null;
    }
}
