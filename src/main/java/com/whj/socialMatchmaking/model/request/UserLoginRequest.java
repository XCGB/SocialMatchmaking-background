package com.whj.socialMatchmaking.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = -7682187501543930543L;

    @ApiModelProperty(value = "用户账号")
    private String userAccount;
    @ApiModelProperty(value = "用户密码")
    private String userPassword;
}
