package com.steel.perf;

import com.steel.perf.integration.snapshot.SnapshotJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 日快照采集集成测试（H2 模拟 ERP 源表与平台 dwd_snapshot）。 */
@SpringBootTest
class SnapshotJobTest {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private SnapshotJob job;

    @BeforeEach
    void setup() {
        jdbc.execute("DROP TABLE IF EXISTS erp_receivable");
        jdbc.execute("DROP TABLE IF EXISTS dwd_snapshot");
        jdbc.execute("CREATE TABLE erp_receivable (tenant_id VARCHAR(32), subject_id BIGINT, amount DECIMAL(18,2))");
        jdbc.execute("CREATE TABLE dwd_snapshot (tenant_id VARCHAR(32), snap_date DATE, snap_type VARCHAR(24), "
                + "subject_id BIGINT, amount DECIMAL(18,2))");
        jdbc.update("INSERT INTO erp_receivable VALUES ('t', 1, 100000)");
        jdbc.update("INSERT INTO erp_receivable VALUES ('t', 2, 50000)");
        jdbc.update("INSERT INTO erp_receivable VALUES ('other', 9, 999999)"); // 他租户
    }

    @Test
    void captureIsTenantScoped() {
        int n = job.capture("t", SnapshotJob.SnapType.RECEIVABLE, LocalDate.of(2026, 6, 30));
        assertEquals(2, n);
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dwd_snapshot WHERE tenant_id='t' AND snap_type='RECEIVABLE'", Integer.class);
        assertEquals(2, cnt);
        Double sum = jdbc.queryForObject(
                "SELECT SUM(amount) FROM dwd_snapshot WHERE tenant_id='t'", Double.class);
        assertEquals(150000d, sum, 1e-6);
        // 他租户未被采集
        Integer other = jdbc.queryForObject(
                "SELECT COUNT(*) FROM dwd_snapshot WHERE tenant_id='other'", Integer.class);
        assertEquals(0, other);
    }
}
