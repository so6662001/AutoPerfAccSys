package com.steel.perf.service;

import com.steel.perf.common.exception.BizException;
import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.repo.jpa.AppealEntity;
import com.steel.perf.repo.jpa.AppealJpaRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 绩效申诉复核服务（perf-appeal）。员工发起申诉 → 复核裁定（调整/维持/升级），租户隔离 + 审计。
 */
@Service
public class AppealService {

    private final AppealJpaRepo repo;
    private final AuditService audit;

    public AppealService(AppealJpaRepo repo, AuditService audit) {
        this.repo = repo;
        this.audit = audit;
    }

    public AppealEntity submit(String period, Long empId, String item, String reason) {
        String tenant = TenantContext.requireTenantId();
        AppealEntity e = new AppealEntity();
        e.setTenantId(tenant);
        e.setAppealNo("AP-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%02d", repo.countByTenantId(tenant) + 1));
        e.setPeriod(period);
        e.setEmpId(empId);
        e.setItem(item);
        e.setReason(reason);
        e.setStatus("PENDING");
        e.setSubmitter(TenantContext.currentUserId());
        e.setCreatedAt(LocalDateTime.now());
        repo.save(e);
        audit.record("APPEAL_SUBMIT", e.getAppealNo(), item + "：" + reason);
        return e;
    }

    /** 复核裁定。decision: ADJUST / KEEP / ESCALATE。 */
    public AppealEntity adjudicate(String appealNo, String decision, String note) {
        String tenant = TenantContext.requireTenantId();
        AppealEntity e = repo.findByTenantIdAndAppealNo(tenant, appealNo)
                .orElseThrow(() -> new BizException(404, "申诉单不存在: " + appealNo));
        if (!"PENDING".equals(e.getStatus())) {
            throw new BizException("申诉单已处理: " + e.getStatus());
        }
        String status = switch (decision) {
            case "ADJUST" -> "ADJUSTED";
            case "KEEP" -> "KEPT";
            case "ESCALATE" -> "ESCALATED";
            default -> throw new BizException("非法裁定: " + decision);
        };
        e.setStatus(status);
        e.setVerdict(note);
        e.setReviewer(TenantContext.currentUserId());
        repo.save(e);
        audit.record("APPEAL_ADJUDICATE", appealNo, decision + "：" + note);
        return e;
    }

    public List<AppealEntity> list() {
        return repo.findByTenantId(TenantContext.requireTenantId(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
