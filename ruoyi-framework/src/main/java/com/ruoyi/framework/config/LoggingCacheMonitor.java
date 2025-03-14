package com.ruoyi.framework.config;

import com.alicp.jetcache.CacheMonitor;
import com.alicp.jetcache.event.CacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingCacheMonitor implements CacheMonitor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingCacheMonitor.class);


    @Override
    public void afterOperation(CacheEvent event) {
        if (event != null) {
            logger.info("缓存命中 ----------------->> 键: {}", event.getCache());
        }
    }
}