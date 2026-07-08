package com.steel.perf.repo.jpa;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** 绩效申诉工单（多租户）。状态：PENDING/ADJUSTED/KEPT/ESCALATED。 */
@Entity
@Table(name = "t_appeal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "appeal_no"}))
public class AppealEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 32)
    private String tenantId;
    @Column(name = "appeal_no", nullable = false, length = 64)
    private String appealNo;
    private String period;
    private Long empId;
    private String item;
    @Lob
    @Column(columnDefinition = "text")
    private String reason;
    private String status;
    private String verdict;
    private String submitter;
    private String reviewer;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAppealNo() { return appealNo; }
    public void setAppealNo(String appealNo) { this.appealNo = appealNo; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Long getEmpId() { return empId; }
    public void setEmpId(Long empId) { this.empId = empId; }
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }
    public String getSubmitter() { return submitter; }
    public void setSubmitter(String submitter) { this.submitter = submitter; }
    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
