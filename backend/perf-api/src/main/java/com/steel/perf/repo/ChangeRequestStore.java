package com.steel.perf.repo;

import com.steel.perf.rule.model.ChangeRequest;

import java.util.List;

/**
 * 变更单存储抽象（租户隔离）。提供内存实现与 JPA 实现，接口不变。
 */
public interface ChangeRequestStore {

    void save(String tenantId, ChangeRequest cr);

    ChangeRequest findById(String tenantId, String id);

    List<ChangeRequest> list(String tenantId);

    int nextVersion(String tenantId);
}
