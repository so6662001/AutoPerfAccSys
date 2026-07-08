package com.steel.perf.repo.jpa;

import jakarta.persistence.*;

import java.time.LocalDate;

/** 提成核算单明细（逐单）。period=yyyy-MM，bizDate 用于按日期筛选（不跨月）。 */
@Entity
@Table(name = "t_commission_detail")
public class CommissionDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 32)
    private String tenantId;
    private String period;
    @Column(name = "emp_id")
    private Long empId;
    @Column(name = "biz_date")
    private LocalDate bizDate;
    @Column(name = "order_no", length = 64)
    private String orderNo;
    private String item;
    private double amount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Long getEmpId() { return empId; }
    public void setEmpId(Long empId) { this.empId = empId; }
    public LocalDate getBizDate() { return bizDate; }
    public void setBizDate(LocalDate bizDate) { this.bizDate = bizDate; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}
