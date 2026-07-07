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

## 下一步（M1 起）
- M1：ERP(SQL Server 2008+)只读适配 + DWD + 日快照调度 + 日均/周转率/挂价利润指标。
- M2：核算项/方案落库、跑批（幂等+快照）、个人绩效单下钻。
- M3+：规则治理（发布/三级串签/灰度/审计/版本）、DSL→SQL、四客户配置验证、安全与性能加固。

（里程碑与验收标准详见 `CURSOR开发提示词.md`。）
