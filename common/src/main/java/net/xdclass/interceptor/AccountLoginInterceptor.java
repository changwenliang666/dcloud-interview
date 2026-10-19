package net.xdclass.interceptor;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.vo.AccountDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.util.CommonUtil;
import net.xdclass.util.JsonData;
import net.xdclass.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class AccountLoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<AccountDTO> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //处理option请求
        if (request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return true;
        }

        String token = request.getHeader("token");
        if(StringUtils.isNotBlank(token)){
            Claims claims = JwtUtil.checkTenantAccountLoginJWT(token);
            if(claims == null){
                log.error("token校验失败");
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                return false;
            }


            Long accountId = Long.valueOf(claims.get("accountId")+"");
            String username  = claims.get("username")+"";
            String headImg = claims.get("headImg")+"";

            AccountDTO accountDTO = AccountDTO.builder().headImg(headImg).id(accountId).username(username).build();
            threadLocal.set(accountDTO);
            return true;
        }

        //未登录
        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}

