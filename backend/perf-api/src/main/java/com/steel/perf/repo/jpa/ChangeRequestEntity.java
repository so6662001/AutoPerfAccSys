package com.steel.perf.repo.jpa;

import jakarta.persistence.*;

/**
 * 变更单持久化实体（复杂字段以 JSON 列存储）。多租户 tenant_id + 单号唯一。
 */
@Entity
@Table(name = "t_change_request",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "cr_no"}))
public class ChangeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 32)
    private String tenantId;

    @Column(name = "cr_no", nullable = false, length = 64)
    private String crNo;

    private int ver;
    private String submitter;
    private String effMode;
    private String effLabel;
    private String scopeLabel;
    private String status;
    private int curStep;

    @Lob
    @Column(columnDefinition = "text")
    private String chainJson;
    @Lob
    @Column(columnDefinition = "text")
    private String detailsJson;
    @Lob
    @Column(columnDefinition = "text")
    private String paramsJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCrNo() { return crNo; }
    public void setCrNo(String crNo) { this.crNo = crNo; }
    public int getVer() { return ver; }
    public void setVer(int ver) { this.ver = ver; }
    public String getSubmitter() { return submitter; }
    public void setSubmitter(String submitter) { this.submitter = submitter; }
    public String getEffMode() { return effMode; }
    public void setEffMode(String effMode) { this.effMode = effMode; }
    public String getEffLabel() { return effLabel; }
    public void setEffLabel(String effLabel) { this.effLabel = effLabel; }
    public String getScopeLabel() { return scopeLabel; }
    public void setScopeLabel(String scopeLabel) { this.scopeLabel = scopeLabel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCurStep() { return curStep; }
    public void setCurStep(int curStep) { this.curStep = curStep; }
    public String getChainJson() { return chainJson; }
    public void setChainJson(String chainJson) { this.chainJson = chainJson; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
    public String getParamsJson() { return paramsJson; }
    public void setParamsJson(String paramsJson) { this.paramsJson = paramsJson; }
}
