package extend.common.wangtiansoft;

/**
 * Created by cosmos on 17-9-11.
 */

public class EncryDao {

    public static final String KEY = "1p622z194LH5POS9";

    public static String encry(String content, String key) {
        try{
            String aesKey = key;
            String data = AES.encryptToBase64(content, aesKey);
            return data;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }


    public static String deEncry(String object, String key){
        try {
            // 获取aesKEY
            String enkey = /*RSA.decrypt(bean.encryptKey, key);*/key;
            String result = AES.decryptFromBase64(object, enkey);
            return result;
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String deEncryDirectly(String object, String key, boolean base64){
        try {
            // 获取aesKEY
            String result = base64 ? AES.decryptFromBase64(object, key) : AES.decryptDirectly(object, key);
            return result;
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getCommonEncryptedKey(){
        return RandomUtil.getRandom(16);
    }
}
