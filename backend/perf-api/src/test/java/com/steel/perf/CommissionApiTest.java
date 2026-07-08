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
class CommissionApiTest {

    @Autowired
    private MockMvc mvc;

    private void add(String tenant, String date, String item, double amt) throws Exception {
        mvc.perform(post("/api/commission/details").header("X-Tenant-Id", tenant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"period\":\"2026-06\",\"empId\":1,\"bizDate\":\"" + date
                                + "\",\"orderNo\":\"SO1\",\"item\":\"" + item + "\",\"amount\":" + amt + "}"))
                .andExpect(status().isOk());
    }

    @Test
    void dateRangeFilterWithinMonth() throws Exception {
        add("cm", "2026-06-03", "吨位提成", 100);
        add("cm", "2026-06-15", "挂价利润", 200);
        add("cm", "2026-06-28", "加磅提成", 50);
        // 仅取 1..20 日
        mvc.perform(get("/api/commission/details").param("period", "2026-06").param("from", "1").param("to", "20")
                        .header("X-Tenant-Id", "cm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2)); // 03、15 命中；28 不在区间
    }

    @Test
    void crossMonthDateRejected() throws Exception {
        mvc.perform(post("/api/commission/details").header("X-Tenant-Id", "cm2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"period\":\"2026-06\",\"empId\":1,\"bizDate\":\"2026-07-01\",\"orderNo\":\"x\",\"item\":\"y\",\"amount\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400)); // 跨月被拒
    }
}
