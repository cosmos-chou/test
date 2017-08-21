package extend.manager;

import android.content.Context;

import java.util.List;

import extend.plugn.ExPlugnMoudle;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;

/**
 * Created by cosmos on 17-8-21.
 */

public class ExtendManager {

    public static void init(Context context){
        setMyExtensionModule();
    }
    public static void setMyExtensionModule() {
        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        IExtensionModule defaultModule = null;
        if (moduleList != null) {
            for (IExtensionModule module : moduleList) {
                if (module instanceof DefaultExtensionModule) {
                    defaultModule = module;
                    break;
                }
            }
            if (defaultModule != null) {
                RongExtensionManager.getInstance().unregisterExtensionModule(defaultModule);
                RongExtensionManager.getInstance().registerExtensionModule(new ExPlugnMoudle());
            }
        }
    }
}
