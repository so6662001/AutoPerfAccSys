package com.steel.perf;

import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.integration.MetricQueryService;
import com.steel.perf.integration.dsl.MetricQuerySpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 取数链路集成测试：以 H2 模拟 ERP 表 dwd_sales_detail，验证
 * DSL → 参数化 SQL → 执行，且强制 tenant_id 隔离与条件过滤生效。
 */
@SpringBootTest
class MetricQueryServiceTest {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private MetricQueryService svc;

    @BeforeEach
    void setup() {
        jdbc.execute("DROP TABLE IF EXISTS dwd_sales_detail");
        jdbc.execute("CREATE TABLE dwd_sales_detail (tenant_id VARCHAR(32), emp_id BIGINT, "
                + "biz_type VARCHAR(32), qty DECIMAL(16,3))");
        jdbc.update("INSERT INTO dwd_sales_detail VALUES ('t','1','现货',10)");
        jdbc.update("INSERT INTO dwd_sales_detail VALUES ('t','1','现货',20)");
        jdbc.update("INSERT INTO dwd_sales_detail VALUES ('t','2','现货',5)");
        jdbc.update("INSERT INTO dwd_sales_detail VALUES ('t','1','直发',100)"); // 条件过滤掉
        jdbc.update("INSERT INTO dwd_sales_detail VALUES ('other','1','现货',999)"); // 他租户隔离掉
        TenantContext.set("t", "u", Set.of("HR"));
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void dslToSqlExecuteWithTenantAndCondition() {
        MetricQuerySpec spec = new MetricQuerySpec(
                "dwd_sales_detail", MetricQuerySpec.Agg.SUM, "qty", MetricQuerySpec.GroupBy.EMP,
                List.of(new MetricQuerySpec.Condition("biz_type", "=", "bizType")));
        List<Map<String, Object>> rows = svc.query(spec, Map.of("bizType", "现货"));
        // 期望：emp1=30(10+20，仅现货)，emp2=5；直发100与他租户999被过滤
        Map<Object, Double> byEmp = new java.util.HashMap<>();
        for (Map<String, Object> r : rows) {
            byEmp.put(String.valueOf(r.get("SUBJECT")), ((Number) r.get("VAL")).doubleValue());
        }
        assertEquals(2, byEmp.size());
        assertEquals(30d, byEmp.get("1"), 1e-6);
        assertEquals(5d, byEmp.get("2"), 1e-6);
    }
}
