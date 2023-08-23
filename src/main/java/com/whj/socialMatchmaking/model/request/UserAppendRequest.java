package com.whj.socialMatchmaking.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Baldwin
 * @createTime: 2023-06-08 13:41
 * @description:
 */
@Data
public class UserAppendRequest implements Serializable {

    private static final long serialVersionUID = -6483301656456608268L;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户账号
     */
    private String userAccount;


    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;


}
