package com.whj.socialMatchmaking.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: Baldwin
 * @createTime: 2023-07-25 14:48
 * @description:
 */
@Data
public class TeamUpdateRequest implements Serializable {
    private static final long serialVersionUID = -5803825832968189309L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

//    /**
//     * 最大人数
//     */
//    private Integer maxNum;

    /**
     * 密码
     */
    private String password;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 过期时间
     */
    private Date expireTime;

}
