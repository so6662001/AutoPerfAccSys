package com.steel.perf.integration;

import com.steel.perf.integration.snapshot.SnapshotJob;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 每日快照采集（JDBC 实现）。从 ERP 只读读取当日余额，写入平台 dwd_snapshot，
 * 作为日均库存/应收、周转率、逐日计息的基础。租户隔离。
 * <p>演示 RECEIVABLE：从 erp_receivable 读入 → dwd_snapshot。生产按类型对接各 ERP 源表。
 */
@Component
public class JdbcSnapshotJob implements SnapshotJob {

    private final JdbcTemplate jdbc;

    public JdbcSnapshotJob(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int capture(String tenantId, SnapType type, LocalDate date) {
        String sourceTable = switch (type) {
            case RECEIVABLE -> "erp_receivable";
            case INVENTORY -> "erp_inventory";
            case ADVANCE_RECEIPT -> "erp_advance_receipt";
            case PAYABLE -> "erp_payable";
        };
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT subject_id, amount FROM " + sourceTable + " WHERE tenant_id = ?", tenantId);
        int n = 0;
        for (Map<String, Object> r : rows) {
            jdbc.update("INSERT INTO dwd_snapshot(tenant_id, snap_date, snap_type, subject_id, amount) "
                            + "VALUES (?,?,?,?,?)",
                    tenantId, java.sql.Date.valueOf(date), type.name(),
                    ((Number) r.get("SUBJECT_ID")).longValue(), r.get("AMOUNT"));
            n++;
        }
        return n;
    }
}
