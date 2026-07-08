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

## 下一步
- 持久层：MyBatis + 平台库(MySQL) 落地仓储、只读 ERP(SQL Server) 数据源接 DSL→SQL 执行、日快照 @Scheduled。
- 前端：规则中心/核算工作台/绩效单/审批申诉/员工端 页面对接以上 REST（对应 prototype）。
- M6：安全测试(脱敏/越权集成)、性能压测、可观测、部署；P2 过磅/加工费/新户/合同计息接入。

（里程碑与验收标准详见 `CURSOR开发提示词.md`。）
