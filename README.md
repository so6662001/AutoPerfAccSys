# AutoPerfAccSys · 自动化绩效核算系统

面向钢铁贸易 / 加工 / 生产制造企业的自动化绩效核算平台产品设计方案。

> **开始开发**：将 [`CURSOR开发提示词.md`](./CURSOR开发提示词.md) 作为 Cursor 首条上下文（连同 `docs/` 与 `prototype/`），然后说「请开始编码」即可按规划实现（多租户 · Java + Vue3 · SQL Server 2008+ ERP 只读）。
>
> **编码已启动**：`backend/`（Maven 多模块 Spring Boot，核算引擎内核 + 27 个测试全绿）、`frontend/`（Vue3+Vite+TS 骨架）、`db/platform/`（多租户 DDL）、`docker-compose.yml`。本地运行与进展见 [`docs/DEV-本地运行与进展.md`](./docs/DEV-本地运行与进展.md)。

完整设计文档见 [`docs/`](./docs/README.md)：

- 需求分析与梳理
- 产品方案总览与整体架构
- 采购绩效核算设计
- 销售绩效核算设计
- 数据取数引擎与核算引擎设计
- 数据模型与功能模块清单
- 实施路径、迭代规划与风险
