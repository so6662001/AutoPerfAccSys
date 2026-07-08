package com.steel.perf.repo.jpa;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppealJpaRepo extends JpaRepository<AppealEntity, Long> {

    List<AppealEntity> findByTenantId(String tenantId, Sort sort);

    Optional<AppealEntity> findByTenantIdAndAppealNo(String tenantId, String appealNo);

    long countByTenantId(String tenantId);
}
