# 性能压测

基于 [k6](https://k6.io/) 的负载测试脚本。

## 前置
- 后端已启动（`mvn -pl perf-api spring-boot:run`，或 docker-compose）。
- 安装 k6（`brew install k6` / `apt install k6` / 官方二进制）。

## 运行
```bash
k6 run -e BASE=http://localhost:8080 perf/k6-load.js
```

## 场景与阈值
- 场景：登录取 JWT → `POST /api/calc/run`（核算，校验合计=10860）→ `GET /api/rule/change-requests`。
- 负载：50 并发爬坡 → 稳定 1 分钟 → 收尾。
- 阈值（达标线）：错误率 `< 1%`，`p95 < 500ms`。

## 调优建议（若不达标）
- 后端：数据库连接池(Hikari)、规则/参数缓存(Redis+版本失效)、核算跑批批量化、只读 ERP 走从库/低峰。
- 核算跑批：同租户同周期分布式锁串行、幂等键去重；大结果集分页/流式。
- SQL Server 2008：ROW_NUMBER 分页 + 合理索引；限制返回行数与查询超时。

## 生产可观测
- `/actuator/health`、`/actuator/metrics`（可接 Prometheus/Grafana）。
- 日志含 `traceId` 串联链路。
