package com.steel.perf.repo.jpa;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CommissionDetailJpaRepo extends JpaRepository<CommissionDetailEntity, Long> {

    List<CommissionDetailEntity> findByTenantIdAndPeriodAndBizDateBetween(
            String tenantId, String period, LocalDate from, LocalDate to, Sort sort);
}
