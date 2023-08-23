package com.whj.socialMatchmaking.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: Baldwin
 * @createTime: 2023-07-25 19:46
 * @description: 用户信息封装类（脱敏）
 */
@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1159608972374832860L;

    /**
     * id
     */
    private Long id;

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
     * 个人简介
     */
    private String profile;

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

    /**
     * 标签列表
     */
    private String tags;

    /**
     * 用户状态 0-正常
     */
    private Integer userStatus;

    /**
     * 用户鉴权 0-默认用户  1-管理员
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;
}
