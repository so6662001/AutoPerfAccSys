package com.steel.perf;

import com.steel.perf.repo.jpa.UserEntity;
import com.steel.perf.repo.jpa.UserJpaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 实库用户登录测试（t_user + BCrypt）。 */
@SpringBootTest
@AutoConfigureMockMvc
class UserLoginTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserJpaRepo userRepo;
    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    void seed() {
        userRepo.findByTenantIdAndUsername("acme2", "admin").ifPresent(u -> userRepo.deleteById(u.getId()));
        UserEntity u = new UserEntity();
        u.setTenantId("acme2");
        u.setUsername("admin");
        u.setPassword(encoder.encode("Secret@1"));
        u.setRoles("HR,ADMIN");
        userRepo.save(u);
    }

    @Test
    void dbUserLoginSuccess() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenant\":\"acme2\",\"username\":\"admin\",\"password\":\"Secret@1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    void dbUserWrongPassword() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tenant\":\"acme2\",\"username\":\"admin\",\"password\":\"bad\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
