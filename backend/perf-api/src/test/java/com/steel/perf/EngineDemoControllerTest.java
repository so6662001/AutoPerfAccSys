package com.steel.perf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EngineDemoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void contextLoadsAndFormulaWorks() throws Exception {
        mvc.perform(post("/api/engine/formula")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expression\":\"(deal-list)*qty*alloc\",\"vars\":{\"deal\":4010,\"list\":4000,\"qty\":800,\"alloc\":0.7}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(5600.0));
    }

    @Test
    void whoamiReflectsTenantHeader() throws Exception {
        mvc.perform(get("/api/engine/whoami").header("X-Tenant-Id", "xinqiao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value("xinqiao"));
    }

    @Test
    void tierProgressive() throws Exception {
        mvc.perform(post("/api/engine/tier/step")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"base\":800,\"mode\":\"PROGRESSIVE\",\"rows\":[{\"lower\":0,\"upper\":500,\"factor\":3},{\"lower\":500,\"upper\":1000,\"factor\":5},{\"lower\":1000,\"upper\":null,\"factor\":8}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3000.0));
    }
}
