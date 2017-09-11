package extend.common;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * Created by cosmos on 17-9-11.
 */

public class GsonDao {

    private static Gson mGson = new Gson();


    public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        try{
            return mGson.fromJson(json, classOfT);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
        try{
            return mGson.fromJson(json, typeOfT);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static String toJsonString(Object o){
        return mGson.toJson(o);
    }
}
