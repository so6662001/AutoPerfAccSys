package com.steel.perf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steel.perf.common.exception.BizException;
import com.steel.perf.security.JwtService;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwt = new JwtService("test-secret-32bytes-minimum-length!!", new ObjectMapper());

    @Test
    void roundTrip() {
        String token = jwt.generate("t1", "alice", Set.of("HR", "ADMIN"), 3600);
        JwtService.Claims c = jwt.parse(token);
        assertEquals("t1", c.tenant());
        assertEquals("alice", c.sub());
        assertTrue(c.roles().contains("HR"));
        assertTrue(c.roles().contains("ADMIN"));
    }

    @Test
    void tamperedTokenRejected() {
        String token = jwt.generate("t1", "alice", Set.of("HR"), 3600);
        String tampered = token.substring(0, token.length() - 2) + "xy";
        assertThrows(BizException.class, () -> jwt.parse(tampered));
    }

    @Test
    void expiredTokenRejected() {
        String token = jwt.generate("t1", "alice", Set.of("HR"), -10); // 已过期
        assertThrows(BizException.class, () -> jwt.parse(token));
    }

    @Test
    void differentSecretRejected() {
        String token = jwt.generate("t1", "alice", Set.of("HR"), 3600);
        JwtService other = new JwtService("another-secret-32bytes-minimum-len!!", new ObjectMapper());
        assertThrows(BizException.class, () -> other.parse(token));
    }
}
