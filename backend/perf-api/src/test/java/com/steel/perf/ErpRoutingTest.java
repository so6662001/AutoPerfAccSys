package com.steel.perf;

import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.integration.MetricQueryService;
import com.steel.perf.integration.dsl.MetricQuerySpec;
import com.steel.perf.integration.erp.ErpDataSourceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 每租户 ERP 只读数据源路由测试：为租户 erpA 注册独立 H2「ERP」库，
 * 验证取数路由到该租户自己的 ERP 库（而非默认平台库）。
 */
@SpringBootTest
class ErpRoutingTest {

    @Autowired
    private ErpDataSourceRegistry registry;
    @Autowired
    private MetricQueryService svc;

    private DriverManagerDataSource erpA;

    @BeforeEach
    void setup() {
        erpA = new DriverManagerDataSource(
                "jdbc:h2:mem:erpA;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        erpA.setDriverClassName("org.h2.Driver");
        JdbcTemplate t = new JdbcTemplate(erpA);
        t.execute("DROP TABLE IF EXISTS dwd_sales_detail");
        t.execute("CREATE TABLE dwd_sales_detail (tenant_id VARCHAR(32), emp_id BIGINT, "
                + "biz_type VARCHAR(32), qty DECIMAL(16,3))");
        t.update("INSERT INTO dwd_sales_detail VALUES ('erpA', 7, '现货', 888)");
        registry.register("erpA", erpA);
        TenantContext.set("erpA", "u", Set.of("HR"));
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    @Test
    void queryRoutesToTenantErp() {
        MetricQuerySpec spec = new MetricQuerySpec(
                "dwd_sales_detail", MetricQuerySpec.Agg.SUM, "qty", MetricQuerySpec.GroupBy.EMP, List.of());
        List<Map<String, Object>> rows = svc.query(spec, Map.of());
        // 数据来自 erpA 专属 ERP 库
        assertEquals(1, rows.size());
        assertEquals("7", String.valueOf(rows.get(0).get("SUBJECT")));
        assertEquals(888d, ((Number) rows.get(0).get("VAL")).doubleValue(), 1e-6);
    }
}
