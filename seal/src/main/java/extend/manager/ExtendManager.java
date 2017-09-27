package extend.manager;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import extend.plugn.ExPlugnMoudle;
import extend.plugn.takephoto.VideoMessage;
import extend.plugn.takephoto.VideoMessageItemProvider;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;

/**
 * Created by cosmos on 17-8-21.
 */

public class ExtendManager {
    private static final String DEFAULT_REDPACKET = "com.jrmf360.rylib.modules.JrmfExtensionModule";
    public static void init(Context context) {
        setMyExtensionModule();
        registerExtMessageTypeAndProvider(context);
    }


    public static void registerExtMessageTypeAndProvider(Context context) {
        RongIM.registerMessageType(VideoMessage.class);

        RongIM.registerMessageTemplate(new VideoMessageItemProvider());
    }

    public static void setMyExtensionModule() {
        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        if (moduleList != null) {
            for (IExtensionModule module : new ArrayList<IExtensionModule>(moduleList)) {
                if (module instanceof DefaultExtensionModule) {
                    RongExtensionManager.getInstance().unregisterExtensionModule(module);
                    RongExtensionManager.getInstance().registerExtensionModule(new ExPlugnMoudle());
                }else if( module != null && DEFAULT_REDPACKET.equalsIgnoreCase(module.getClass().getName())){
                    RongExtensionManager.getInstance().unregisterExtensionModule(module);
                }
                System.out.println(module);
            }
        }
    }
}
