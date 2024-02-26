package com.example.projecttemplate.entity.dto;



import com.example.projecttemplate.constant.HttpStatus;
import com.example.projecttemplate.utils.StringUtils;

import java.util.HashMap;

/**
 * 统一接口返回
 * 
 * @author bin
 * @date 2022/08/24
 */
public class ApiResponse extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    public static final String CODE_TAG = "code";

    /** 返回内容 */
    public static final String MSG_TAG = "msg";

    /** 数据对象 */
    public static final String DATA_TAG = "data";
    
    /** 成功信息 */
    public static final String SUCCESS_MSG = "success";
    
    /** 失败信息 */
    public static final String ERROR_MSG = "error";

    /**
     * 初始化一个新创建的 ApiResponse 对象，使其表示一个空消息。
     */
    public ApiResponse()
    {
    }

    /**
     * 初始化一个新创建的 ApiResponse 对象
     *
     * @param code 状态码
     * @param msg 返回内容
     */
    public ApiResponse(int code, String msg)
    {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    /**
     * 初始化一个新创建的 ApiResponse 对象
     *
     * @param code 状态码
     * @param msg 返回内容
     * @param data 数据对象
     */
    public ApiResponse(int code, String msg, Object data)
    {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (StringUtils.isNotNull(data))
        {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static ApiResponse success()
    {
        return ApiResponse.success(SUCCESS_MSG);
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static ApiResponse success(Object data)
    {
        return ApiResponse.success(SUCCESS_MSG, data);
    }

    /**
     * 返回成功消息
     *
     * @param data 返回内容
     * @return 成功消息
     */
    public static ApiResponse success(String data)
    {
        return ApiResponse.success(SUCCESS_MSG, data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static ApiResponse success(String msg, Object data)
    {
        return new ApiResponse(HttpStatus.SUCCESS, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @return
     */
    public static ApiResponse error()
    {
        return ApiResponse.error(ERROR_MSG);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static ApiResponse error(String msg)
    {
        return ApiResponse.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static ApiResponse error(String msg, Object data)
    {
        return new ApiResponse(HttpStatus.ERROR, msg, data);
    }

    /**
     * 返回错误消息
     *
     * @param code 状态码
     * @param msg 返回内容
     * @return 警告消息
     */
    public static ApiResponse error(int code, String msg)
    {
        return new ApiResponse(code, msg, null);
    }

    /**
     * 方便链式调用
     *
     * @param key 键
     * @param value 值
     * @return 数据对象
     */
    @Override
    public ApiResponse put(String key, Object value)
    {
        super.put(key, value);
        return this;
    }

    public static boolean reqSuccess(ApiResponse response){
        return (Integer) response.get(ApiResponse.CODE_TAG) == HttpStatus.SUCCESS;
    }

    public static HashMap<String, Object> getResponseData(ApiResponse response){
        return (HashMap<String, Object>) response.get(ApiResponse.DATA_TAG);
    }

    
}
