package com.steel.perf.web;

import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.common.exception.BizException;
import com.steel.perf.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 认证接口（演示版登录）。生产应校验 t_user + BCrypt，并接入失败锁定/刷新。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwt;
    private final String demoPassword;
    private final long ttlSeconds;

    public AuthController(JwtService jwt,
                          @Value("${perf.auth.demo-password:perf@123}") String demoPassword,
                          @Value("${perf.jwt.ttl-seconds:86400}") long ttlSeconds) {
        this.jwt = jwt;
        this.demoPassword = demoPassword;
        this.ttlSeconds = ttlSeconds;
    }

    public record LoginReq(String tenant, String username, String password, List<String> roles) {
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody LoginReq req) {
        if (req.tenant() == null || req.username() == null || req.password() == null) {
            throw new BizException(400, "租户/用户名/密码必填");
        }
        // 演示：校验固定密码；生产改为 t_user + BCrypt
        if (!demoPassword.equals(req.password())) {
            throw new BizException(401, "用户名或密码错误");
        }
        Set<String> roles = req.roles() == null || req.roles().isEmpty()
                ? Set.of("HR") : Set.copyOf(req.roles());
        String token = jwt.generate(req.tenant(), req.username(), roles, ttlSeconds);
        return ApiResponse.ok(Map.of("token", token, "tenant", req.tenant(),
                "username", req.username(), "roles", roles));
    }
}
