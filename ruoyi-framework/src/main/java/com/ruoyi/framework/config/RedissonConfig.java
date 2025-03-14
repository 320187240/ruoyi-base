package com.ruoyi.framework.config;

import com.alicp.jetcache.anno.CacheType;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    private static final Logger log = LoggerFactory.getLogger(RedissonConfig.class);

    @Bean
    public RedissonClient redissonClient() {
        // 创建 Redisson 配置对象
        Config config = new Config();

        // 配置单节点 Redis 服务器
        config.useSingleServer()
                .setAddress("redis://localhost:6379") // Redis 地址
                .setDatabase(6)                       // 使用的数据库编号
                .setConnectTimeout(10000)            // 连接超时时间（毫秒）
                .setTimeout(3000)                    // 操作超时时间（毫秒）
                .setIdleConnectionTimeout(10000)     // 空闲连接超时时间（毫秒）
                .setRetryAttempts(3)                 // 重试次数
                .setRetryInterval(1500)              // 重试间隔时间（毫秒）
                .setSubscriptionsPerConnection(5)    // 每个连接的订阅数
                .setSubscriptionConnectionMinimumIdleSize(1) // 订阅连接池最小空闲连接数
                .setSubscriptionConnectionPoolSize(50)       // 订阅连接池最大连接数
                .setConnectionMinimumIdleSize(24)           // 最小空闲连接数
                .setConnectionPoolSize(64)                  // 最大连接池大小
                .setDnsMonitoringInterval(5000);            // DNS 监控间隔（毫秒）

        // 设置序列化方式
        config.setCodec(new JsonJacksonCodec());

        // 设置线程数和 Netty 线程数
        config.setThreads(16);
        config.setNettyThreads(32);

        // 设置传输模式
        config.setTransportMode(org.redisson.config.TransportMode.NIO);

        // 返回 Redisson 客户端实例
        return Redisson.create(config);
    }


}