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
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
class RuleAndCalcApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;

    @Test
    void calcRunReturnsPayslip() throws Exception {
        String body = """
            {"planCode":"XPY","version":3,"period":"2026-06",
             "components":[
               {"code":"基本工资","expression":"基数*系数","includeInTotal":true},
               {"code":"吨位提成","expression":"现款量*现款单价+账期量*账期单价","includeInTotal":true},
               {"code":"应收扣减","expression":"-吨位提成*扣比","includeInTotal":true}
             ],
             "context":{"基数":6000,"系数":1.0,"现款量":300,"现款单价":8,"账期量":500,"账期单价":6,"扣比":0.1}}
            """;
        mvc.perform(post("/api/calc/run").header("X-Tenant-Id", "xpy")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(10860.0)) // 6000+5400-540
                .andExpect(jsonPath("$.data.idempotencyKey").isNotEmpty());
    }

    @Test
    void publishBlockedWhenWeightNot100() throws Exception {
        String body = """
            {"planName":"P","scorecardWeightSums":{"销售评分卡":90}}
            """;
        mvc.perform(post("/api/rule/change-requests").header("X-Tenant-Id", "t1")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400)); // 被拦截
    }

    @Test
    void changeRequestThreeLevelSignFlow() throws Exception {
        String submit = """
            {"planName":"销售现货方案","effMode":"NOW","scopeLabel":"全公司",
             "details":["权重调整"],"params":{"销售权重":"40/20/20/20"},
             "scorecardWeightSums":{"销售评分卡":100,"采购评分卡":100}}
            """;
        MvcResult res = mvc.perform(post("/api/rule/change-requests").header("X-Tenant-Id", "t2")
                        .contentType(MediaType.APPLICATION_JSON).content(submit))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        JsonNode node = om.readTree(res.getResponse().getContentAsString());
        String id = node.get("data").get("id").asText();

        approve(id, "陈组长");
        approve(id, "王总监");
        MvcResult last = approve(id, "周总");
        JsonNode lastNode = om.readTree(last.getResponse().getContentAsString());
        assertEquals("EFFECTIVE", lastNode.get("data").get("status").asText());
    }

    @Test
    void submitWritesAuditLog() throws Exception {
        String submit = """
            {"planName":"审计测试","scopeLabel":"全公司","scorecardWeightSums":{"a":100}}
            """;
        mvc.perform(post("/api/rule/change-requests").header("X-Tenant-Id", "audit-t").header("X-User-Id", "李静")
                .contentType(MediaType.APPLICATION_JSON).content(submit)).andExpect(status().isOk());
        mvc.perform(get("/api/audit").header("X-Tenant-Id", "audit-t"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].opType").value("RULE_SUBMIT"))
                .andExpect(jsonPath("$.data[0].opUser").value("李静"));
    }

    @Test
    void calcPersistIsIdempotent() throws Exception {
        String body = """
            {"planCode":"IDEM","version":1,"period":"2026-07","snapshotHash":"fixed",
             "components":[{"code":"a","expression":"10","includeInTotal":true}],
             "context":{}}
            """;
        // 同参数跑两次
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/calc/run").header("X-Tenant-Id", "idem")
                    .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        }
        mvc.perform(get("/api/calc/payslips").param("period", "2026-07").header("X-Tenant-Id", "idem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1)); // 幂等：仅一条
    }

    @Test
    void tenantIsolationListSeparate() throws Exception {
        // t3 提交一单，t4 列表应看不到
        String submit = """
            {"planName":"P","scopeLabel":"全公司","scorecardWeightSums":{"a":100}}
            """;
        mvc.perform(post("/api/rule/change-requests").header("X-Tenant-Id", "t3")
                .contentType(MediaType.APPLICATION_JSON).content(submit)).andExpect(status().isOk());
        mvc.perform(get("/api/rule/change-requests").header("X-Tenant-Id", "t4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    private MvcResult approve(String id, String approver) throws Exception {
        return mvc.perform(post("/api/rule/change-requests/" + id + "/approve").header("X-Tenant-Id", "t2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approver\":\"" + approver + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
    }
}
