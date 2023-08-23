package com.whj.socialMatchmaking.model.request;

import com.whj.socialMatchmaking.model.domain.User;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Baldwin
 * @createTime: 2023-06-07 10:10
 * @description:
 */
@Data
public class UserSelectRequest implements Serializable {
    private static final long serialVersionUID = -2388746843900532463L;

    private Integer current;

    private Integer pageSize;

    private User userParams;

}
