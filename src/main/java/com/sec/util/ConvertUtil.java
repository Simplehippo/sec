package com.sec.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ConvertUtil {
    private ConvertUtil() {
    }

    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        try {
            return mapToObjectDetail(map, clazz);
        } catch (Exception e) {
            throw new RuntimeException("ConvertUtil:mapToObject() -> Map转对象失败！");
        }
    }

    public static Map<String, Object> objectToMap(Object obj) {
        try {
            return objectToMapDetail(obj);
        } catch (Exception e) {
            throw new RuntimeException("ConvertUtil:objectToMap() -> 对象转Map失败！");
        }
    }


    private static <T> T mapToObjectDetail(Map<String, Object> map, Class<T> clazz) throws Exception {
        T newObj = clazz.newInstance();

        if (map == null || map.size() == 0) {
            return newObj;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            field.set(newObj, map.get(field.getName()));
        }

        return newObj;
    }


    private static Map<String, Object> objectToMapDetail(Object obj) throws Exception {
        if(obj == null){
            return null;
        }

        Map<String, Object> map = new HashMap<>(16);

        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }

        return map;
    }
}
