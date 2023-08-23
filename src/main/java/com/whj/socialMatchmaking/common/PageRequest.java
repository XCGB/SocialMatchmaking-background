package com.whj.socialMatchmaking.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Baldwin
 * @createTime: 2023-07-24 18:13
 * @description: 分页请求类
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 4095479204561585351L;

    /**
     * 页面大小
     */
    protected int pageSize;

    /**
     * 当前页
     */
    protected int pageNum;

}
