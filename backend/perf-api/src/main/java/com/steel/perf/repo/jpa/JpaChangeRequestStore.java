package com.steel.perf.repo.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steel.perf.common.exception.BizException;
import com.steel.perf.repo.ChangeRequestStore;
import com.steel.perf.rule.model.ApprovalStep;
import com.steel.perf.rule.model.ChangeRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 变更单 JPA 存储实现（@Primary，租户隔离，复杂字段 JSON 化）。
 */
@Repository
@Primary
public class JpaChangeRequestStore implements ChangeRequestStore {

    private final ChangeRequestJpaRepo repo;
    private final ObjectMapper om;

    public JpaChangeRequestStore(ChangeRequestJpaRepo repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    @Override
    public void save(String tenantId, ChangeRequest cr) {
        ChangeRequestEntity e = repo.findByTenantIdAndCrNo(tenantId, cr.getId())
                .orElseGet(ChangeRequestEntity::new);
        e.setTenantId(tenantId);
        e.setCrNo(cr.getId());
        e.setVer(cr.getVer());
        e.setSubmitter(cr.getSubmitter());
        e.setEffMode(cr.getEffMode().name());
        e.setEffLabel(cr.getEffLabel());
        e.setScopeLabel(cr.getScopeLabel());
        e.setStatus(cr.getStatus().name());
        e.setCurStep(cr.getCurStep());
        e.setChainJson(write(cr.getChain()));
        e.setDetailsJson(write(cr.getDetails()));
        e.setParamsJson(write(cr.getParams()));
        repo.save(e);
    }

    @Override
    public ChangeRequest findById(String tenantId, String id) {
        return repo.findByTenantIdAndCrNo(tenantId, id).map(this::toDomain).orElse(null);
    }

    @Override
    public List<ChangeRequest> list(String tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public int nextVersion(String tenantId) {
        return repo.maxVer(tenantId) + 1;
    }

    private ChangeRequest toDomain(ChangeRequestEntity e) {
        ChangeRequest cr = new ChangeRequest();
        cr.setId(e.getCrNo());
        cr.setVer(e.getVer());
        cr.setSubmitter(e.getSubmitter());
        cr.setEffMode(ChangeRequest.EffMode.valueOf(e.getEffMode()));
        cr.setEffLabel(e.getEffLabel());
        cr.setScopeLabel(e.getScopeLabel());
        cr.setStatus(ChangeRequest.Status.valueOf(e.getStatus()));
        cr.setCurStep(e.getCurStep());
        cr.setChain(read(e.getChainJson(), new TypeReference<List<ApprovalStep>>() {}));
        cr.setDetails(read(e.getDetailsJson(), new TypeReference<List<String>>() {}));
        cr.setParams(read(e.getParamsJson(), new TypeReference<Map<String, String>>() {}));
        return cr;
    }

    private String write(Object o) {
        try {
            return o == null ? null : om.writeValueAsString(o);
        } catch (Exception ex) {
            throw new BizException("变更单序列化失败: " + ex.getMessage());
        }
    }

    private <T> T read(String json, TypeReference<T> type) {
        try {
            return json == null ? null : om.readValue(json, type);
        } catch (Exception ex) {
            throw new BizException("变更单反序列化失败: " + ex.getMessage());
        }
    }
}
