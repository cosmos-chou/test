package extend.plugn;

import java.util.ArrayList;
import java.util.List;

import extend.plugn.takephoto.TakePhotoPlugn;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imlib.model.Conversation;

/**
 * Created by cosmos on 17-8-21.
 */

public class ExPlugnMoudle extends DefaultExtensionModule {
    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        List<IPluginModule> moudles = super.getPluginModules(conversationType);
        List<IPluginModule> moudlesEx = new ArrayList<IPluginModule>();

        IPluginModule moudle;
        for(int i = 0, size = moudles.size(); i < size; i ++){
            moudle = moudles.get(i);
            if(moudle instanceof ImagePlugin){
                moudlesEx.add(moudle);
                moudlesEx.add(new TakePhotoPlugn());
            }/*else if( !(moudle instanceof FilePlugin || moudle instanceof ))*/else{
                moudlesEx.add(moudle);
            }
        }
        return moudlesEx;
    }
}
