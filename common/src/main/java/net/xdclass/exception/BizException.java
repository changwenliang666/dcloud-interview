package net.xdclass.exception;

import net.xdclass.enums.BizCodeEnum;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群
 * @Version 1.0
 **/

public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    private final String msg;


    public BizException(BizCodeEnum bizCodeEnum){
        super(bizCodeEnum.getMessage());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
    }

    public BizException(BizCodeEnum bizCodeEnum, Exception e){
        super(bizCodeEnum.getMessage(), e);
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }





}
