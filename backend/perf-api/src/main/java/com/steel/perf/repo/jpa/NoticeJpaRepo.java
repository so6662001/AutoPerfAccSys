package com.steel.perf.repo.jpa;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeJpaRepo extends JpaRepository<NoticeEntity, Long> {

    List<NoticeEntity> findByTenantId(String tenantId, Sort sort);
}
