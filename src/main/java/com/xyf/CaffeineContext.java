package com.xyf;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class CaffeineContext {
    private volatile static Cache<String,Object> cache;

    private CaffeineContext(Long time , Integer size){
        cache = Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(size)
                .expireAfterWrite(time, TimeUnit.SECONDS)
                .build();
    }
    public static Cache<String,Object> newInstance(Long time,Integer size){
        if (cache == null){
            synchronized (CaffeineContext.class){
                if (cache == null){
                    CaffeineContext cache_context = new CaffeineContext(time,size);
                }
            }
        }
        return cache;
    }
    public static Cache<String,Object> getCache(){
        return cache;
    }
}
