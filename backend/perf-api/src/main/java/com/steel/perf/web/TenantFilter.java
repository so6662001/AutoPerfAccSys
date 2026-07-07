package com.steel.perf.web;

import com.steel.perf.common.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 多租户上下文过滤器（M0 脚手架版）。
 * 从请求头解析租户/用户/角色写入 {@link TenantContext}，请求结束清理，避免 ThreadLocal 泄漏。
 * <p>M0 之后将替换为从 JWT 解析（见 SecurityConfig 计划）。
 */
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String tenant = header(request, "X-Tenant-Id", "demo");
            String user = header(request, "X-User-Id", "anonymous");
            String rolesRaw = header(request, "X-Roles", "GUEST");
            Set<String> roles = new HashSet<>(Arrays.asList(rolesRaw.split(",")));
            TenantContext.set(tenant, user, roles);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String header(HttpServletRequest req, String name, String def) {
        String v = req.getHeader(name);
        return (v == null || v.isBlank()) ? def : v;
    }
}
