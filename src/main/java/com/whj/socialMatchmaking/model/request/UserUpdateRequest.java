package com.whj.socialMatchmaking.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Baldwin
 * @createTime: 2023-06-11 09:23
 * @description:
 */
@Data

public class UserUpdateRequest  implements Serializable {

    private static final long serialVersionUID = 1200855077553079070L;

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
