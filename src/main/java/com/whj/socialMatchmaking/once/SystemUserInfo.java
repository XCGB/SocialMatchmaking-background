package com.whj.socialMatchmaking.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: Baldwin
 * @createTime: 2023-07-17 19:34
 * @description: 导入用户类
 */
@Data
@EqualsAndHashCode
public class SystemUserInfo {
    /**
     * 姓名
     */
    @ExcelProperty("姓名")
    private String username;

    /**
     * 性别
     */
    @ExcelProperty("性别")
    private String gender;


}
