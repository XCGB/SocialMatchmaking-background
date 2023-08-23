package com.whj.socialMatchmaking.common;

/**
 * @author: Baldwin
 * @createTime: 2023-07-13 19:30
 * @description: 返回结果工具类
 */
public class ResultUtils {
    /**
     * 成功
     * @param data 数据
     * @param <T> 泛型
     * @return 泛型类数据
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "success");
    }

    /**
     * 失败
     * @param errorCode 错误状态码
     * @return 通用返回数据类型
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     *
     * @param errorCode 错误状态码
     * @param description 状态码描述（详情）
     * @return 通用返回数据类型
     */
    public static BaseResponse error(ErrorCode errorCode,String description) {
        return new BaseResponse(errorCode.getCode(),errorCode.getMessage(),description);
    }

    /**
     * 失败
     * @param errorCode 错误状态码
     * @param message 状态码信息
     * @param description 状态码描述（详情）
     * @return 通用返回数据类型
     */
    public static BaseResponse error(ErrorCode errorCode,String message,String description) {
        return new BaseResponse(errorCode.getCode(),message,description);
    }

    /**
     * 失败
     * @param code 自定义状态码
     * @param message 状态码信息
     * @param description 状态码描述（详情）
     * @return 通用返回数据类型
     */
    public static BaseResponse error(int code,String message,String description) {
        return new BaseResponse(code,null,message,description);
    }

}
