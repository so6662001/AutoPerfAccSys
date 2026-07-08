package com.steel.perf.integration.snapshot;

import java.time.LocalDate;

/**
 * 每日快照采集任务（骨架）。
 * 每日定时从 ERP 拉取库存/应收/预收/应付预付余额快照，写入 dwd_snapshot，
 * 作为日均库存/应收、周转率与逐日计息的基础。
 * <p>调度与只读数据源在 perf-api 用 @Scheduled + 只读 DataSource 装配（M1 后续）。
 */
public interface SnapshotJob {

    enum SnapType { INVENTORY, RECEIVABLE, ADVANCE_RECEIPT, PAYABLE }

    /**
     * 采集指定租户、指定日期、指定类型的快照。
     *
     * @return 采集到的快照条数
     */
    int capture(String tenantId, SnapType type, LocalDate date);
}
