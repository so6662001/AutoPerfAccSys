package com.steel.perf.web;

import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.common.exception.BizException;
import com.steel.perf.repo.jpa.UserEntity;
import com.steel.perf.repo.jpa.UserJpaRepo;
import com.steel.perf.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证接口（演示版登录）。生产应校验 t_user + BCrypt，并接入失败锁定/刷新。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwt;
    private final UserJpaRepo userRepo;
    private final PasswordEncoder encoder;
    private final String demoPassword;
    private final long ttlSeconds;

    public AuthController(JwtService jwt, UserJpaRepo userRepo, PasswordEncoder encoder,
                          @Value("${perf.auth.demo-password:perf@123}") String demoPassword,
                          @Value("${perf.jwt.ttl-seconds:86400}") long ttlSeconds) {
        this.jwt = jwt;
        this.userRepo = userRepo;
        this.encoder = encoder;
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
        Set<String> roles;
        Optional<UserEntity> userOpt = userRepo.findByTenantIdAndUsername(req.tenant(), req.username());
        if (userOpt.isPresent()) {
            // 实库校验：BCrypt 密码 + 用户角色
            UserEntity u = userOpt.get();
            if (u.getStatus() != 1 || !encoder.matches(req.password(), u.getPassword())) {
                throw new BizException(401, "用户名或密码错误");
            }
            roles = u.getRoles() == null ? Set.of() : Arrays.stream(u.getRoles().split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        } else {
            // 无此用户时回退演示密码（dev；生产可关闭）
            if (!demoPassword.equals(req.password())) {
                throw new BizException(401, "用户名或密码错误");
            }
            roles = req.roles() == null || req.roles().isEmpty() ? Set.of("HR") : Set.copyOf(req.roles());
        }
        String token = jwt.generate(req.tenant(), req.username(), roles, ttlSeconds);
        return ApiResponse.ok(Map.of("token", token, "tenant", req.tenant(),
                "username", req.username(), "roles", roles));
    }
}
