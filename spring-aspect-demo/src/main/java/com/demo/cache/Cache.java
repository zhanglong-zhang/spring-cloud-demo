package com.demo.cache;

import java.util.concurrent.TimeUnit;

/**
 * @author ZhangLong on 2019/10/22  9:02 下午
 * @version V1.0
 */
public interface Cache<K, V> {

    void cache(K key, V val, Long expire, TimeUnit timeUnit);

    void dealSoftCache();

     V get(K key);

    default V get(K key, String param){
        System.out.println(param);
        return get(key);
    }
}
