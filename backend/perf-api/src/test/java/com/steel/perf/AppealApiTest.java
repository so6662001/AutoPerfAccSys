package com.steel.perf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppealApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;

    @Test
    void submitThenAdjudicate() throws Exception {
        String submit = """
            {"period":"2026-06","empId":271,"item":"挂价利润","reason":"成交价取数偏低"}
            """;
        MvcResult res = mvc.perform(post("/api/appeal").header("X-Tenant-Id", "ap").header("X-User-Id", "孙磊")
                        .contentType(MediaType.APPLICATION_JSON).content(submit))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        JsonNode n = om.readTree(res.getResponse().getContentAsString());
        String no = n.get("data").get("appealNo").asText();

        mvc.perform(post("/api/appeal/" + no + "/adjudicate").header("X-Tenant-Id", "ap").header("X-User-Id", "李静")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"ADJUST\",\"note\":\"修正取数并重算\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ADJUSTED"))
                .andExpect(jsonPath("$.data.reviewer").value("李静"));
    }

    @Test
    void tenantIsolation() throws Exception {
        mvc.perform(post("/api/appeal").header("X-Tenant-Id", "ap1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"period\":\"2026-06\",\"empId\":1,\"item\":\"x\",\"reason\":\"y\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/appeal").header("X-Tenant-Id", "ap2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
