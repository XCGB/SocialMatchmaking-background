package com.whj.socialMatchmaking.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Baldwin
 * @createTime: 2023-07-25 14:48
 * @description:
 */
@Data
public class TeamJoinRequest implements Serializable {
    private static final long serialVersionUID = -5803825832968189309L;
    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;



}
