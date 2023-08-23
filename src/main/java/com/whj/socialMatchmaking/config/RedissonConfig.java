package com.whj.socialMatchmaking.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author: Baldwin
 * @createTime: 2023-07-23 15:35
 * @description: Redis配置类
 */
@Component
@Data
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfig {
    private String host;
    private String port;

    /**
     * redisson客户
     *
     * @return {@link RedissonClient}
     */
    @Bean
    public RedissonClient redissonClient(){
        // 1. 创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s",host,port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(1);
        // 2. 创建示例
        return Redisson.create(config);
    }
}
