package com.xyf;


import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/*
    Caffeine的工厂类
 */
public class CaffeineFactory implements FactoryBean<Object> {
    private Class<?> type;

    @Override
    public Object getObject() throws Exception {
        Object o = Proxy.newProxyInstance(type.getClassLoader(), type.getInterfaces(), (Object proxy, Method method, Object[] args) -> {
            //获取到字段的key，去Caffeine中查询对应的Value
            String key = "";
            //获取所有字段
            Field[] fields = type.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(CaffeineAnnotation.class)) {
                    CaffeineAnnotation annotation = field.getAnnotation(CaffeineAnnotation.class);
                    key = annotation.key();
                }
            }
            return CaffeineContext.getCache().getIfPresent(key);
        });
        return o;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
