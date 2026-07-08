package com.steel.perf.repo.jpa;

import jakarta.persistence.*;

/**
 * 绩效单持久化实体。idempotencyKey 唯一 → 同租户/周期/版本/快照重跑不重复入库（幂等）。
 */
@Entity
@Table(name = "t_payslip",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "idempotency_key"}))
public class PayslipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 32)
    private String tenantId;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    private String period;
    private String planCode;
    private int planVersion;
    private double total;

    @Lob
    @Column(columnDefinition = "text")
    private String detailJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public int getPlanVersion() { return planVersion; }
    public void setPlanVersion(int planVersion) { this.planVersion = planVersion; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getDetailJson() { return detailJson; }
    public void setDetailJson(String detailJson) { this.detailJson = detailJson; }
}
