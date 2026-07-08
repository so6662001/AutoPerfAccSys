package com.steel.perf.service;

import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.repo.jpa.AuditLogEntity;
import com.steel.perf.repo.jpa.AuditLogJpaRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/** 审计服务：记录敏感操作（规则变更/审批/核算/导出/越权），租户隔离。 */
@Service
public class AuditService {

    private final AuditLogJpaRepo repo;

    public AuditService(AuditLogJpaRepo repo) {
        this.repo = repo;
    }

    public void record(String opType, String target, String detail) {
        AuditLogEntity e = new AuditLogEntity();
        e.setTenantId(TenantContext.requireTenantId());
        e.setOpUser(TenantContext.currentUserId());
        e.setOpType(opType);
        e.setTarget(target);
        e.setDetail(detail);
        e.setOpAt(LocalDateTime.now());
        repo.save(e);
    }

    public List<AuditLogEntity> list() {
        return repo.findByTenantId(TenantContext.requireTenantId(), Sort.by(Sort.Direction.DESC, "opAt"));
    }
}
