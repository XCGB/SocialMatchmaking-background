package com.whj.socialMatchmaking.constant;

/**
 * @author: Baldwin
 * @createTime: 2023-07-13 19:34
 * @description: 用户常量
 */
public interface UserConstant {
    /**
     * 用户登录态的键
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;

    /**
     * 推荐用户信息的Redis键
     */
    String REDIS_KEY_PREFIX = "HJ:user:recommend:%s";
}
