package com.steel.perf.repo.jpa;

import jakarta.persistence.*;

/** 用户（多租户 + BCrypt 密码 + 角色）。 */
@Entity
@Table(name = "t_user",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "username"}))
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, length = 32)
    private String tenantId;
    @Column(nullable = false, length = 64)
    private String username;
    @Column(nullable = false, length = 100)
    private String password;   // BCrypt
    @Column(length = 256)
    private String roles;      // 逗号分隔
    private int status = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
