package com.whj.socialMatchmaking.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Baldwin
 * @createTime: 2023-07-25 14:48
 * @description:
 */
@Data
public class TeamQuitRequest implements Serializable {
    private static final long serialVersionUID = -5803825832968189309L;
    /**
     * id
     */
    private Long teamId;


}
