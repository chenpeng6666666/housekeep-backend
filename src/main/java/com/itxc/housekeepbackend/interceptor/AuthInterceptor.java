package com.itxc.housekeepbackend.interceptor;

import cn.hutool.json.JSONUtil;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseContext;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.itxc.housekeepbackend.constant.UserConstant.*;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 放行跨域预检 OPTIONS 请求！
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 如果不是映射到 Controller 方法的请求（如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 2. 尝试从方法上获取注解
        RequireAuth auth = handlerMethod.getMethodAnnotation(RequireAuth.class);
        // 3. 如果方法上没有，尝试从类上获取注解
        if (auth == null) {
            auth = handlerMethod.getBeanType().getAnnotation(RequireAuth.class);
        }

        // 4. 【核心逻辑】如果类和方法上都没有注解，说明是公开接口（白名单），直接放行！
        if (auth == null) {
            return true;
        }

        // 5. 如果有注解，获取需要的角色类型  通过注解参数获取实际的业务需求
        String role = auth.value();
        Object id = null;

        // 6. 根据不同的角色要求，去 Session 里拿不同的钥匙
        if (ADMIN.equals(role)) {
            id = request.getSession().getAttribute("admin");
        } else if (COMPANY.equals(role)) {
            id = request.getSession().getAttribute("employeeId");
        } else if (USER.equals(role)) {
            id = request.getSession().getAttribute("userId");
        }

        // 7. 如果拿到了 ID，说明登录合法，放入 ThreadLocal 并放行
        if (id != null) {
            BaseContext.setCurrentId((Long) id);
            return true;
        }

        // 8. 没拿到 ID，拦截请求，返回 401 错误
        log.warn("越权访问被拦截：请求URI = {}, 需要角色 = {}", request.getRequestURI(), role);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSONUtil.toJsonStr(ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "未登录或无权限访问")));
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 9. 🚨 同样极其重要：请求处理完毕，清理 ThreadLocal，防止内存泄漏
        BaseContext.remove();
    }



}