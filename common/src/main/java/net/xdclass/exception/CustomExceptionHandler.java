package net.xdclass.exception;

import lombok.extern.slf4j.Slf4j;
import net.xdclass.util.JsonData;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

/**
 * 小滴课堂,愿景：让技术不再难学
 *
 * @Description
 * @Author 二当家小D
 * @Remark 有问题直接联系我，源码-笔记-技术交流群
 * @Version 1.0
 **/

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(value = BizException.class)
    public JsonData handleBizException(BizException e){
        log.error("[业务异常]{}",e.getMsg());
        return JsonData.buildCodeAndMsg(e.getCode(),e.getMsg());
    }

    /**
     * @RequestBody参数校验异常（JSON格式）
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public JsonData handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            if (!errors.isEmpty()) {
                FieldError fieldError = (FieldError) errors.get(0);
                log.error("[参数校验异常]字段:{}, 错误:{}", fieldError.getField(), fieldError.getDefaultMessage());
                return JsonData.buildCodeAndMsg(1, fieldError.getDefaultMessage());
            }
        }
        return JsonData.buildCodeAndMsg(1,"参数校验失败");
    }


    /**
     * @RequestParam/@PathVariable单个参数校验异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public JsonData handleConstraintViolationException(ConstraintViolationException e){
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        if (!violations.isEmpty()) {
            ConstraintViolation<?> violation = violations.iterator().next();
            String message = violation.getMessage();
            return JsonData.buildCodeAndMsg(1, message);
        }
        return JsonData.buildCodeAndMsg(1,"参数约束校验失败");
    }

    /**
     * 兜底异常处理
     */
    @ExceptionHandler(value = Exception.class)
    public JsonData handleException(Exception e){
        log.error("[系统异常]", e);
        return JsonData.buildError("系统异常，请稍后重试");
    }

}
