package com.steel.perf.config;

import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.repo.ChangeRequestStore;
import com.steel.perf.rule.ChangeRequestService;
import com.steel.perf.rule.model.ChangeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 开发/演示数据播种（默认关闭；perf.seed.enabled=true 开启，如 docker-compose）。
 * 建 ERP 演示表与样例数据，并生成一张待审批变更单，便于前端联调演示。测试环境不启用。
 */
@Component
@ConditionalOnProperty(name = "perf.seed.enabled", havingValue = "true")
public class DevSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeeder.class);
    private static final String TENANT = "demo";

    private final JdbcTemplate jdbc;
    private final ChangeRequestService crSvc;
    private final ChangeRequestStore crStore;

    public DevSeeder(JdbcTemplate jdbc, ChangeRequestService crSvc, ChangeRequestStore crStore) {
        this.jdbc = jdbc;
        this.crSvc = crSvc;
        this.crStore = crStore;
    }

    @Override
    public void run(String... args) {
        // ERP 演示表（平台库作默认 ERP 回退）
        jdbc.execute("CREATE TABLE IF NOT EXISTS dwd_sales_detail (tenant_id VARCHAR(32), emp_id BIGINT, "
                + "biz_type VARCHAR(32), qty DECIMAL(16,3))");
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dwd_sales_detail WHERE tenant_id = ?", Integer.class, TENANT);
        if (cnt == null || cnt == 0) {
            jdbc.update("INSERT INTO dwd_sales_detail VALUES (?,?,?,?)", TENANT, 1L, "现货", 300);
            jdbc.update("INSERT INTO dwd_sales_detail VALUES (?,?,?,?)", TENANT, 1L, "现货", 500);
            jdbc.update("INSERT INTO dwd_sales_detail VALUES (?,?,?,?)", TENANT, 2L, "直发", 200);
        }
        // 演示变更单
        try {
            TenantContext.set(TENANT, "system", Set.of("HR"));
            if (crStore.list(TENANT).isEmpty()) {
                int ver = crStore.nextVersion(TENANT);
                ChangeRequest cr = new ChangeRequest();
                cr.setId("CR-DEMO-" + ver);
                cr.setVer(ver);
                cr.setSubmitter("system");
                cr.setScopeLabel("全公司");
                cr.setChain(crSvc.defaultChain());
                cr.setDetails(List.of("演示：销售现货方案权重调整"));
                cr.setParams(Map.of("销售权重", "40/20/20/20"));
                crStore.save(TENANT, cr);
            }
            log.info("演示数据播种完成 tenant={}", TENANT);
        } finally {
            TenantContext.clear();
        }
    }
}
