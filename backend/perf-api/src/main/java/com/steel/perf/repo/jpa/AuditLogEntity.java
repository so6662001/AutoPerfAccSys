package com.steel.perf.repo.jpa;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** 审计日志：登录/规则变更/审批/核算/发放/导出/越权尝试 全量留痕（多租户）。 */
@Entity
@Table(name = "t_audit_log")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 32)
    private String tenantId;
    private String opUser;
    private String opType;
    private String target;
    @Lob
    @Column(columnDefinition = "text")
    private String detail;
    private LocalDateTime opAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOpUser() { return opUser; }
    public void setOpUser(String opUser) { this.opUser = opUser; }
    public String getOpType() { return opType; }
    public void setOpType(String opType) { this.opType = opType; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public LocalDateTime getOpAt() { return opAt; }
    public void setOpAt(LocalDateTime opAt) { this.opAt = opAt; }
}
