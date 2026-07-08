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
class MetricAndActuatorApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void contractInterestEndpoint() throws Exception {
        String body = """
            {"dayRate":0.0005,"segments":[
              {"amount":200000,"days":4},{"amount":100000,"days":6},
              {"amount":200000,"days":6},{"amount":300000,"days":10},{"amount":200000,"days":10}]}
            """;
        mvc.perform(post("/api/metric/interest/contract").header("X-Tenant-Id", "t")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(3800.0)); // 岳洋通期货计息
    }

    @Test
    void dailyInterestEndpoint() throws Exception {
        String body = "{\"dayRate\":0.0005,\"dailyBalances\":[500000,500000,500000]}";
        mvc.perform(post("/api/metric/interest/daily").header("X-Tenant-Id", "t")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(750.0)); // 500000*0.0005*3
    }

    @Test
    void actuatorHealthUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
