package com.steel.perf.web;

import com.steel.perf.common.exception.BizException;
import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.security.JwtService;
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
 * 多租户上下文过滤器。优先解析 Authorization: Bearer JWT（生产鉴权）；
 * 无 Token 时回退请求头（dev/联调）。请求结束清理 ThreadLocal，避免泄漏。
 */
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public TenantFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                try {
                    JwtService.Claims c = jwt.parse(auth.substring(7));
                    TenantContext.set(c.tenant(), c.sub(), c.roles());
                } catch (BizException e) {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"" + e.getMessage() + "\",\"data\":null}");
                    return;
                }
            } else {
                String tenant = header(request, "X-Tenant-Id", "demo");
                String user = header(request, "X-User-Id", "anonymous");
                String rolesRaw = header(request, "X-Roles", "GUEST");
                Set<String> roles = new HashSet<>(Arrays.asList(rolesRaw.split(",")));
                TenantContext.set(tenant, user, roles);
            }
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
