# 开发进展与本地运行指南

> 编码已启动，按 [`CURSOR开发提示词.md`](../CURSOR开发提示词.md) 的里程碑推进。

## 已完成（M0 · 脚手架 + 核算引擎内核）

- **后端多模块**（Maven，Java 17，Spring Boot 3.2）：`backend/`
  - `perf-common`：统一返回体 `ApiResponse`、`BizException`、**多租户上下文 `TenantContext`**（含跨租户越权拦截）。
  - `perf-engine`：**平台基础能力内核**
    - `scope/ScopeResolver`：多维参数作用域解析（最具体命中）。
    - `tier/TierEngine`：阶梯(整体/累进) + 门槛 + 阶跃 + 封顶。
    - `coefficient/CoefficientMapper`：达成率→系数 / →固定金额。
    - `cost/CostEngine`：结算方式驱动成本 + 毛利调整。
    - `formula/ExpressionEngine`：沙箱公式引擎（递归下降解析，白名单函数，禁止 eval）。
  - `perf-api`：Spring Boot 启动、安全配置骨架、**多租户过滤器 `TenantFilter`**、全局异常、引擎演示接口。
  - **测试**：27 个用例全绿（引擎单测 + 客户场景 + API 集成），含 岳洋通配货=830、名亿吨位=9000、超扑越挂价=5600/基本工资档=3500、信桥负值单价、公式注入/越权防护等。
- **平台库 DDL**：`db/platform/V1__init.sql`（多租户 + 四客户字段 + 规则/变更单/明细/审计）。
- **前端骨架**（Vue3 + Vite + TS + Pinia + Element Plus）：`frontend/`，含引擎联调页 `views/EngineDemo.vue`、租户头注入。
- **容器化**：`backend/Dockerfile`、`frontend/Dockerfile`、根 `docker-compose.yml`（MySQL + Redis + 后端 + 前端）。

## 本地运行

### 后端（已验证）
```bash
cd backend
mvn test          # 运行全部测试（27 个）
mvn -pl perf-api spring-boot:run   # 启动 API (:8080)
# 试算：
curl -X POST localhost:8080/api/engine/formula -H 'Content-Type: application/json' \
  -d '{"expression":"(deal-list)*qty*alloc","vars":{"deal":4010,"list":4000,"qty":800,"alloc":0.7}}'
# => {"code":0,"message":"success","data":5600.0}
```

### 前端
```bash
cd frontend
npm install
npm run dev       # http://localhost:5173 （/api 代理到 :8080）
```

### 一键（需 Docker）
```bash
docker-compose up --build   # mysql:3306 / redis:6379 / backend:8080 / frontend:5173
```

## 已完成（M1 · 数据集成与指标）

- **perf-metric（指标计算，纯逻辑可测）**：
  - `MetricService`：日均库存/应收、存货/应收周转率与周转天数、挂价利润、业务净利润、成本重估(能力性收益)、逐日计息。
  - `ContractInterestService`：**期货合同滚动计息**（定金/货物分笔，金额×天数×日利率）——复现岳洋通示例利息 ¥3,800。
- **perf-integration（ERP 只读集成 + 安全取数）**：
  - `SafeSqlCompiler`：**取数 DSL→SQL 安全编译器**——表/字段白名单 + 标识符正则 + 运算符白名单 + 值参数绑定 + 强制 tenant_id + 仅单条 SELECT。含 6 项注入/越权拒绝测试。
  - `SqlServerDialect`：SQL Server **2008 兼容分页**（ROW_NUMBER）。
  - `ErpReader` 只读取数接口、`SnapshotJob` 日快照采集骨架。
- **测试**：累计 41 个全绿（引擎24 + 指标5 + 集成9 + API3）。

## 已完成（M2 · 核算编排引擎）

- **perf-calc**：
  - `CalcEngine`：核算项**依赖拓扑排序**执行（核算项可引用指标/参数/其他核算项）、公式引擎逐项计算、**逐项下钻追溯**（记录所用变量取值）、汇总（计入项之和或自定义汇总公式）。
  - `IdempotencyKey`：租户+周期+方案版本+快照哈希 SHA-256 幂等键（可复算）。
  - 循环依赖检测、核算项编码去重。
  - **端到端复现超扑越绩效单 = ¥20,835**（基本工资/吨位提成/挂价所得/预收计息/应收扣减，含"应收扣减依赖吨位提成"的顺序与下钻）。
- **测试**：累计 45 个全绿（引擎24 + 指标5 + 集成9 + 核算4 + API3）。

## 已完成（M3 · 规则治理）

- **perf-rule**：
  - `PublishValidator`：发布硬校验（评分卡权重合计必须 = 100%，否则拦截）。
  - `ChangeRequestService`：变更单生命周期——**三级串签**（部门主管→HR负责人[或签]→分管领导），支持**委托代理**、**加签**（动态插入审批人）；全部通过后按生效方式**立即生效/待生效**；**到期生效**；**灰度转全量推广**（重新串签）；退回终止。
  - `ParamDiff`：版本参数差异对比（时间轴 diff）。
- **测试**：累计 53 个全绿（引擎24 + 指标5 + 集成9 + 核算4 + 治理8 + API3）。

## 已完成（M5 验收点 · 四客户方案可配置跑通验证）

- `perf-calc/FourClientValidationTest`：用平台基础能力（作用域/阶梯/成本/系数）+ 核算编排，以**纯配置**复现四家真实客户关键数值，证明"无需为每家写死代码"：
  - 超扑越·现货 = **¥20,835**
  - 岳洋通·配货库 = **¥830**（销量+差价×团队系数+加工+新户）
  - 信桥·现货10天内 = **¥2,100**（吨位900 + 利润绩效1200，吨利润封顶50）
  - 名亿 = 吨位提成9000（整体档位）+ 自营收益10000 + 结算成本50000 + 达成率96%→系数0.8
- **测试**：累计 57 个全绿（引擎24 + 指标5 + 集成9 + 核算8 + 治理8 + API3）。

## 已完成（M4 · REST 服务层）

- **perf-api 接口层**（服务→仓储→REST 全链路，租户隔离）：
  - `RuleController`：变更单 提交(发布权重硬校验)/列表/审批(三级串签)/退回/委托/加签/到期生效/灰度推广/版本diff。
  - `CalcController`：`/api/calc/run` 按方案+上下文执行核算，返回带下钻与幂等键的绩效单。
  - `ChangeRequestRepository`：内存租户隔离仓储（生产将替换为 MyBatis + 平台库，接口不变）。
  - `EngineBeans`：注册 metric/calc/rule/校验/diff 组件为 Bean。
- **集成测试**（MockMvc）：核算合计、发布权重不足被拦截、变更单三级串签走到 EFFECTIVE、**租户隔离(t3提交/t4列表为空)**。
- 修复：父 POM 编译加 `-parameters`（非 spring-boot-starter-parent 时 @PathVariable 名称解析所需）。
- **测试**：累计 61 个全绿（引擎24 + 指标5 + 集成9 + 核算8 + 治理8 + API7）。

## 已完成（M4 · 前端联调打通）

- **前端页面**（Vue3 + TS + Element Plus，`vue-tsc` 类型检查通过 + Vite 构建成功）：
  - `AppLayout`：侧边导航布局。
  - `CalcWorkbench.vue`（核算工作台）：编辑核算项+上下文 → `/api/calc/run` → 展示绩效单合计、幂等键、**逐项下钻**（默认复现超扑越 ¥20,835）。
  - `RuleCenter.vue`（规则变更审批）：提交变更单(权重校验) → 列表 → 三级串签通过/退回/到期生效/灰度推广，状态标签联动。
  - `api/calc.ts`、`api/rule.ts`：类型化 REST 封装；`http.ts` 注入租户头。
- **系统端到端打通**：Vue3 页面 → REST API → 核算/治理引擎。前端 `npm run build` 通过。

## 已完成（持久层 · JPA + H2/MySQL）

- **JPA 持久化**（Spring Data JPA；默认 H2 内存库，生产 profile=prod 用 MySQL）：
  - `ChangeRequestEntity` + `JpaChangeRequestStore`(@Primary)：变更单持久化（复杂字段 JSON 列），租户隔离，`ChangeRequestStore` 接口保留内存实现作降级。
  - `PayslipEntity` + 幂等入库：`idempotencyKey` 唯一约束，**同参数重跑不重复入库**（集成测试验证：跑两次列表仅 1 条）。
  - `application.yml` H2 默认；`application-prod.yml.example` 提供 MySQL + ERP 只读配置样例。
- **应用可真实启动并持久化**（H2），Spring Boot 上下文含 JPA/事务。
- **测试**：累计 62 个全绿（引擎24 + 指标5 + 集成9 + 核算8 + 治理8 + API8）。

## 已完成（ERP 取数执行链）

- `JdbcErpReader`：`NamedParameterJdbcTemplate` 执行只读参数化查询（设最大行数/超时）；生产注入独立 ERP 只读数据源(SQL Server)，dev 用平台数据源作演示。
- `MetricQueryService`：编译取数 DSL(`SafeSqlCompiler`) → 强制注入当前租户 `tenantId` → 校验参数齐全 → 执行，返回按核算对象聚合结果。
- **集成测试**（H2 模拟 ERP 表 `dwd_sales_detail`）：`SUM(qty) GROUP BY emp` + 条件 `biz_type=现货` → 验证 DSL→SQL→执行、**参数绑定、tenant_id 隔离、条件过滤**（他租户与非现货数据被正确过滤）。
- **测试**：累计 63 个全绿（引擎24 + 指标5 + 集成9 + 核算8 + 治理8 + API9）。

## 已完成（日快照 + M6 加固首批）

- **审计日志**：`AuditLogEntity`/`AuditService`/`AuditController`，规则提交/审批写入审计（租户+操作人+类型+时间），`/api/audit` 查询；集成测试验证提交产生审计记录。
- **字段级脱敏**：`MaskingService` 按角色（HR/绩效专员/分管领导等）控制薪酬金额可见/掩码；单元测试覆盖授权与非授权。
- **日快照采集**：`JdbcSnapshotJob`（ERP 只读源 → 平台 `dwd_snapshot`，租户隔离）+ `SnapshotScheduler`（默认关闭，`perf.snapshot.enabled=true` 开启，每日 00:30）；集成测试验证采集条数/汇总/租户隔离。
- `@EnableScheduling` 开启定时能力。
- **测试**：累计 67 个全绿（引擎24 + 指标5 + 集成9 + 核算8 + 治理8 + API13）。

## 已完成（P2 专项能力·引擎）

- `WeighService`（过磅）：加磅利润提成（非负）、**0.5 进位处理**（不满0.5凑满0.5/满0.5进位）、**点数自营利润**((同规格最高挂价−30)×单重/1000 定价 0.5进位后与成交价求差)。
- `IncentiveService`（激励）：新户按月固定奖励(前3月且达标)、累计吨量阶段奖励(外地新户≥300吨→1元/吨)、按个计提(地推纯新客户10元/个)、加工费提成(3%)。
- **测试**：累计 73 个全绿（引擎30 + 指标5 + 集成9 + 核算8 + 治理8 + API13）。

## 已完成（可观测性 + 前端审计页）

- **可观测性**：`TraceFilter` 为每请求生成/透传 `traceId`（MDC + 响应头 `X-Trace-Id`），日志 pattern 含 traceId 串联链路；集成测试验证响应头存在。
- **前端审计查询页** `AuditView.vue`：对接 `/api/audit`，表格展示审计留痕；导航新增入口；`npm run build` 通过。
- **测试**：累计 74 个全绿（引擎30 + 指标5 + 集成9 + 核算8 + 治理8 + API14）。

## 已完成（每租户 ERP 只读数据源路由）

- `ErpDataSourceRegistry`：按租户维护各自 ERP 只读数据源（生产从 t_tenant.erp_conn_enc 解密构建注册），未注册回退默认。
- `ErpRoutingDataSource`：按当前租户上下文路由 ERP 连接；`JdbcErpReader` 内部持有（非 Spring DataSource Bean，避免与平台库自动装配歧义）。
- **集成测试**：为租户 erpA 注册独立 H2「ERP」库，验证取数**路由到该租户自己的 ERP 库**（返回 erpA 专属数据 888），实现多租户"各自 SQL Server 只读"取数隔离。
- **测试**：累计 75 个全绿（引擎30 + 指标5 + 集成9 + 核算8 + 治理8 + API15）。

## 已完成（指标/计息 REST + actuator）

- `MetricController`：`/api/metric/query`（取数 DSL→安全SQL→租户路由执行）、`/api/metric/interest/daily`（逐日计息）、`/api/metric/interest/contract`（期货合同滚动计息）。
- **actuator**：暴露 `health,info,metrics`；集成测试验证 `/actuator/health` UP、合同计息接口=3800、逐日计息=750。
- **测试**：累计 78 个全绿（引擎30 + 指标5 + 集成9 + 核算8 + 治理8 + API18）。

## 已完成（CI + 演示数据）

- **CI 质量门禁**（`.github/workflows/ci.yml`）：后端 `mvn -B test`（JDK17）+ 前端 `npm ci && npm run build`（Node22，vue-tsc 类型检查）；本地已验证 `npm ci`+build 通过。
- **演示数据播种** `DevSeeder`（默认关闭，`perf.seed.enabled=true` 开启，docker-compose 已启用）：建 ERP 演示表+样例数据+一张待审批变更单，便于前端联调演示；测试环境不启用（78 测试不受影响）。

## 已完成（申诉复核 perf-appeal + K8s + 前端页）

- **申诉复核后端**：`AppealEntity`/`AppealJpaRepo`/`AppealService`/`AppealController`——员工发起申诉 → 复核裁定(ADJUST/KEEP/ESCALATE)，租户隔离 + 审计留痕；集成测试(提交→裁定ADJUSTED、租户隔离)。
- **K8s 部署清单** `k8s/deploy.yaml`（Namespace/Secret/ConfigMap + MySQL/Redis/后端/前端 + Ingress + 探针/限额）。
- **前端页面**：指标计息试算 `MetricView`、绩效申诉复核 `AppealView`（+ 之前的核算/规则/审计/公式）；`npm run build` 通过。
- **测试**：累计 80 个全绿（引擎30 + 指标5 + 集成9 + 核算8 + 治理8 + API20）。

## 下一步
- 性能压测报告；前端审批链可视化、员工端消息页、沙盘/明细报表页。

（里程碑与验收标准详见 `CURSOR开发提示词.md`。）
