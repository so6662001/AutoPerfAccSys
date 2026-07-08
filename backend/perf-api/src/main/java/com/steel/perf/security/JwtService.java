package com.steel.perf.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steel.perf.common.exception.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * 轻量 JWT（HS256，无第三方依赖）：签发/校验含 租户/用户/角色/过期 的令牌。
 * 生产可替换为标准库并接入密钥轮换/KMS。
 */
@Service
public class JwtService {

    private final byte[] secret;
    private final ObjectMapper om;
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();

    public JwtService(@Value("${perf.jwt.secret:dev-secret-change-me-please-32bytes!!}") String secret,
                      ObjectMapper om) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.om = om;
    }

    public record Claims(String tenant, String sub, Set<String> roles, long exp) {
    }

    public String generate(String tenant, String sub, Set<String> roles, long ttlSeconds) {
        try {
            String header = B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("tenant", tenant);
            payload.put("sub", sub);
            payload.put("roles", roles);
            payload.put("exp", Instant.now().getEpochSecond() + ttlSeconds);
            String body = B64.encodeToString(om.writeValueAsBytes(payload));
            String signingInput = header + "." + body;
            String sig = B64.encodeToString(hmac(signingInput));
            return signingInput + "." + sig;
        } catch (Exception e) {
            throw new BizException("JWT 签发失败: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Claims parse(String token) {
        String[] parts = token == null ? new String[0] : token.split("\\.");
        if (parts.length != 3) {
            throw new BizException(401, "无效令牌");
        }
        byte[] expected = hmac(parts[0] + "." + parts[1]);
        byte[] actual = B64D.decode(parts[2]);
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new BizException(401, "令牌签名校验失败");
        }
        try {
            Map<String, Object> payload = om.readValue(B64D.decode(parts[1]), Map.class);
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > exp) {
                throw new BizException(401, "令牌已过期");
            }
            Set<String> roles = new HashSet<>((List<String>) payload.getOrDefault("roles", List.of()));
            return new Claims((String) payload.get("tenant"), (String) payload.get("sub"), roles, exp);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(401, "令牌解析失败");
        }
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new BizException("JWT 签名计算失败");
        }
    }
}
