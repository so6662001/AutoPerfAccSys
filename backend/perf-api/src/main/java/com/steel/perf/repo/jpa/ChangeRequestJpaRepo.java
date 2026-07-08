package com.steel.perf.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** 变更单 Spring Data 仓储；所有查询按 tenant_id 过滤（多租户隔离）。 */
public interface ChangeRequestJpaRepo extends JpaRepository<ChangeRequestEntity, Long> {

    List<ChangeRequestEntity> findByTenantId(String tenantId);

    Optional<ChangeRequestEntity> findByTenantIdAndCrNo(String tenantId, String crNo);

    @org.springframework.data.jpa.repository.Query(
            "select coalesce(max(e.ver), 3) from ChangeRequestEntity e where e.tenantId = :tenantId")
    int maxVer(String tenantId);
}
