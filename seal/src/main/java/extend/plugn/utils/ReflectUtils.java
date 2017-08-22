package extend.plugn.utils;

import java.lang.reflect.Field;

/**
 * Created by cosmos on 17-8-22.
 */

public class ReflectUtils {

    public static void setValue(Class<?> clazz, String name , Object value, Object target){

        try{
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static Object getValue(Class<?> clazz, String name, Object target){
        try{
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }
}
