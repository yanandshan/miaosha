package com.qfedu.service;

public interface LocalCacheService {
    //存方法
    void setCommonCache(String key, Object value);
    //取方法
    Object getFromCommonCache(String key);
}
