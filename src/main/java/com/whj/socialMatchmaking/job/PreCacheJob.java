package com.whj.socialMatchmaking.job;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whj.socialMatchmaking.model.domain.User;
import com.whj.socialMatchmaking.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.whj.socialMatchmaking.constant.UserConstant.REDIS_KEY_PREFIX;

/**
 * @author: Baldwin
 * @createTime: 2023-07-23 14:14
 * @description: 对高权限用户查看的数据进行缓存预热
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private List<Long> mainUserList = Arrays.asList(1L);

    /**
     * 缓存预热
     * 提前将热门或者常用的数据加载到缓存中
     */
    @Scheduled(cron = "0 57 4 * * ?")
    public void doCacheRecommendUser(){
        RLock lock = redissonClient.getLock("HJ:precacheJob:doCache:lock");
        try {
            // 通过Redisson抢锁(-1 表示看门狗模式）
            if (lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getLock:" + Thread.currentThread().getName());
                for (Long userId : mainUserList){
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userList = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format(REDIS_KEY_PREFIX, userId);
                    // 写入缓存
                    try {
                        stringRedisTemplate.opsForValue().set(redisKey, JSONUtil.toJsonStr(userList),36000, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        } finally {
            if (lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }

}
