package com.steel.perf.repo;

import com.steel.perf.rule.model.ChangeRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 变更单内存实现（租户隔离，保留作为无 DB 场景/参考实现）。
 * 生产默认使用 {@link JpaChangeRequestStore}（@Primary）。
 */
@Repository
public class ChangeRequestRepository implements ChangeRequestStore {

    private final Map<String, Map<String, ChangeRequest>> store = new ConcurrentHashMap<>();

    private Map<String, ChangeRequest> tenantMap(String tenantId) {
        return store.computeIfAbsent(tenantId, k -> new ConcurrentHashMap<>());
    }

    @Override
    public void save(String tenantId, ChangeRequest cr) {
        tenantMap(tenantId).put(cr.getId(), cr);
    }

    @Override
    public ChangeRequest findById(String tenantId, String id) {
        return tenantMap(tenantId).get(id);
    }

    @Override
    public List<ChangeRequest> list(String tenantId) {
        return new ArrayList<>(tenantMap(tenantId).values());
    }

    @Override
    public int nextVersion(String tenantId) {
        int max = 3;
        for (ChangeRequest cr : tenantMap(tenantId).values()) {
            max = Math.max(max, cr.getVer());
        }
        return max + 1;
    }
}
