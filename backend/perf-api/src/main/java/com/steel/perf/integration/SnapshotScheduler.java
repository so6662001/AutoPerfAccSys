package com.steel.perf.integration;

import com.steel.perf.integration.snapshot.SnapshotJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 每日快照定时调度（默认关闭；生产 perf.snapshot.enabled=true 开启）。
 * 每日 00:30 遍历租户采集库存/应收/预收/应付预付余额。此处为骨架，租户遍历接租户服务。
 */
@Component
@ConditionalOnProperty(name = "perf.snapshot.enabled", havingValue = "true")
public class SnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(SnapshotScheduler.class);

    private final SnapshotJob job;

    public SnapshotScheduler(SnapshotJob job) {
        this.job = job;
    }

    @Scheduled(cron = "0 30 0 * * ?")
    public void daily() {
        LocalDate today = LocalDate.now();
        // 生产：遍历所有启用租户；此处示意单租户占位
        log.info("每日快照采集开始 date={}", today);
        // for (tenant : tenantService.enabledTenants()) { for (type) job.capture(tenant, type, today); }
    }
}
