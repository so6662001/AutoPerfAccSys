package com.steel.perf.repo;

import com.steel.perf.rule.model.ChangeRequest;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 变更单仓储（M4 内存实现，租户隔离）。
 * <p>生产实现将替换为 MyBatis + 平台库（表 t_change_request，强制 tenant_id 过滤），接口不变。
 */
@Repository
public class ChangeRequestRepository {

    private final Map<String, Map<String, ChangeRequest>> store = new ConcurrentHashMap<>();

    private Map<String, ChangeRequest> tenantMap(String tenantId) {
        return store.computeIfAbsent(tenantId, k -> new ConcurrentHashMap<>());
    }

    public void save(String tenantId, ChangeRequest cr) {
        tenantMap(tenantId).put(cr.getId(), cr);
    }

    public ChangeRequest findById(String tenantId, String id) {
        return tenantMap(tenantId).get(id);
    }

    public List<ChangeRequest> list(String tenantId) {
        return new ArrayList<>(tenantMap(tenantId).values());
    }

    /** 下一个版本号（该租户当前最大版本 + 1，基线 3）。 */
    public int nextVersion(String tenantId) {
        int max = 3;
        for (ChangeRequest cr : tenantMap(tenantId).values()) {
            max = Math.max(max, cr.getVer());
        }
        return max + 1;
    }
}
