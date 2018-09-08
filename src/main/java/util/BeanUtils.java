package util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangmingquan on 2018/9/7.
 */
public class BeanUtils {
    public static Object getProVlaue(Object source,String key){
        if(source instanceof Map){
            return ((Map) source).get(key);
        }else{//通过反射获取对象的属性
            key = key.substring(0, 1).toUpperCase() + key.substring(1);
            try {
                return MethodUtils.invokeMethod(source,"get"+key);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("通过反射获取属性值失败");
            }
        }
    }

    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        Preconditions.checkNotNull(obj);
        Map<String, Object> map = new HashMap<>();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }
}
