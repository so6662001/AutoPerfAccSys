package com.steel.perf.repo.jpa;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogJpaRepo extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByTenantId(String tenantId, Sort sort);
}
