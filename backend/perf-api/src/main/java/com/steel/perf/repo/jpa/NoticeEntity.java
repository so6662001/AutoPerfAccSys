package com.steel.perf.repo.jpa;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** 员工端通知（规则生效等），多租户广播。 */
@Entity
@Table(name = "t_notice")
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 32)
    private String tenantId;
    private String title;
    @Lob
    @Column(columnDefinition = "text")
    private String body;
    @Column(name = "cr_id", length = 64)
    private String crId;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getCrId() { return crId; }
    public void setCrId(String crId) { this.crId = crId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
