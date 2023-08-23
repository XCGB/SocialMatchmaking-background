package com.whj.socialMatchmaking.model.dto;

import com.whj.socialMatchmaking.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


/**
 * @author: Baldwin
 * @createTime: 2023-07-24 16:43
 * @description: 队伍请求封装类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TeamQueryRequest extends PageRequest {
    private Long id;

    /**
     * id列表
     */
    private List<Long> idList;

    /**
     * 搜索关键词（同时搜索队伍名称和描述）
     */
    private String searchText;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 密码
     */
    private String password;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


}
