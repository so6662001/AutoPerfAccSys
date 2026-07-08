# 钢绩云 · 后端（steel-perf）

钢铁行业**多租户** SaaS 绩效核算系统后端。Java 17 + Spring Boot 3.2 + Maven 多模块 + JPA。

## 模块

| 模块 | 职责 |
| --- | --- |
| `perf-common` | 统一返回体、异常、**多租户上下文 TenantContext**（越权拦截） |
| `perf-engine` | 核算引擎内核：多维参数作用域、阶梯/门槛/阶跃/封顶、达成率映射、成本口径、公式沙箱、过磅(加磅/点数)、激励 |
| `perf-metric` | 指标：日均/周转率/挂价利润/净利润/成本重估/计息、期货合同滚动计息 |
| `perf-integration` | ERP 只读集成：取数 DSL→SQL 安全编译（注入防护）、SQL Server 2008 分页、每租户 ERP 数据源路由、日快照 |
| `perf-calc` | 核算编排：核算项拓扑排序、逐项下钻、幂等键 |
| `perf-rule` | 规则治理：发布硬校验、三级串签(或签/委托/加签)、灰度/推广、版本 diff |
| `perf-api` | REST 聚合、JWT 认证、RBAC、多租户过滤、持久化(JPA)、审计、脱敏、通知、可观测 |

## 运行

```bash
mvn test                                   # 全部单元/集成测试（H2）
mvn -pl perf-api spring-boot:run           # 启动 API（默认 H2 内存库，:8080）
# 演示数据： -Dspring-boot.run.arguments=--perf.seed.enabled=true
```

- API 文档：`/swagger-ui.html`、`/v3/api-docs`
- 健康检查：`/actuator/health`
- 生产：`spring.profiles.active=prod`（MySQL），见 `perf-api/src/main/resources/application-prod.yml.example`

## 认证与多租户

- 登录：`POST /api/auth/login {tenant,username,password}` → JWT（演示密码 `perf@123`，或 `t_user`+BCrypt）。
- 请求头 `Authorization: Bearer <token>`（或 dev 用 `X-Tenant-Id`/`X-Roles`）。
- 多租户双层隔离：平台库 `tenant_id` + ERP 源库按租户路由；跨租户访问拒绝并审计。

## 主要接口

| 前缀 | 说明 |
| --- | --- |
| `/api/auth` | 登录发 JWT |
| `/api/calc` | 核算 run（绩效单+下钻+幂等）、payslips 查询、sandbox 政策沙盘 |
| `/api/rule` | 变更单提交/三级串签审批/退回/委托/加签/到期生效/灰度推广/版本 diff |
| `/api/metric` | 取数 DSL 查询、逐日计息、合同滚动计息 |
| `/api/commission` | 绩效明细表（逐单、按日期不跨月） |
| `/api/appeal` | 申诉提交/复核裁定 |
| `/api/notice` | 员工端消息 |
| `/api/audit` | 合规审计查询 |

## 安全达标

JWT 认证 + RBAC 方法级授权、多租户强制隔离、取数 SQL 白名单+参数绑定(防注入)、公式引擎沙箱、字段级脱敏、审计留痕、traceId 链路、幂等复算(引擎+DB 唯一约束)。

## 测试

`mvn test` 覆盖 94 个用例：引擎/指标/取数(含注入防护)/核算(含四客户可配置复现)/治理/API(含租户隔离、RBAC、JWT、幂等、明细不跨月、通知)。
