package com.steel.perf.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 绩效单仓储；按 tenant_id 隔离，idempotencyKey 去重。 */
public interface PayslipJpaRepo extends JpaRepository<PayslipEntity, Long> {

    Optional<PayslipEntity> findByTenantIdAndIdempotencyKey(String tenantId, String idempotencyKey);

    List<PayslipEntity> findByTenantIdAndPeriod(String tenantId, String period);
}
