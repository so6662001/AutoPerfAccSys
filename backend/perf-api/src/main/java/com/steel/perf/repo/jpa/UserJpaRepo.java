package com.steel.perf.repo.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepo extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByTenantIdAndUsername(String tenantId, String username);
}
