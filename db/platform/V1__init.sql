-- ============================================================
-- 钢铁行业多租户绩效核算系统 · 平台库初始化 (MySQL 8)
-- 多租户：共享库 + tenant_id 判别列；所有查询强制带 tenant_id（MyBatis 拦截器）。
-- 通用字段：tenant_id / created_by / created_at / updated_at / version(乐观锁) / deleted(软删)
-- ============================================================

-- 租户
CREATE TABLE t_tenant (
  id           VARCHAR(32)  NOT NULL COMMENT '租户ID',
  name         VARCHAR(128) NOT NULL COMMENT '租户名称',
  status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
  erp_conn_enc VARCHAR(1024) NULL COMMENT 'ERP(SQLServer)只读连接串(加密)',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) COMMENT='租户';

-- 用户 / 角色（RBAC）
CREATE TABLE t_user (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  tenant_id   VARCHAR(32)  NOT NULL,
  username    VARCHAR(64)  NOT NULL,
  password    VARCHAR(100) NOT NULL COMMENT 'BCrypt',
  emp_id      BIGINT       NULL,
  status      TINYINT      NOT NULL DEFAULT 1,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user (tenant_id, username)
) COMMENT='用户';

CREATE TABLE t_role (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  tenant_id  VARCHAR(32) NOT NULL,
  code       VARCHAR(64) NOT NULL,
  name       VARCHAR(64) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role (tenant_id, code)
) COMMENT='角色';

CREATE TABLE t_user_role (
  tenant_id VARCHAR(32) NOT NULL,
  user_id   BIGINT      NOT NULL,
  role_id   BIGINT      NOT NULL,
  PRIMARY KEY (user_id, role_id)
) COMMENT='用户角色';

-- 组织 / 岗位 / 员工 / 分支机构 / 库别 / 业务类型 / 客户 / 品规
CREATE TABLE t_org (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  name VARCHAR(128) NOT NULL, parent_id BIGINT NULL, PRIMARY KEY (id)
) COMMENT='组织';

CREATE TABLE t_branch (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  name VARCHAR(128) NOT NULL, PRIMARY KEY (id)
) COMMENT='分支机构';

CREATE TABLE t_employee (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  emp_no VARCHAR(64) NOT NULL, name VARCHAR(64) NOT NULL,
  org_id BIGINT NULL, branch_id BIGINT NULL, position VARCHAR(64) NULL COMMENT '岗位:采购/销售/实习等',
  perf_base DECIMAL(14,2) NULL COMMENT '绩效基数', status TINYINT NOT NULL DEFAULT 1,
  PRIMARY KEY (id), UNIQUE KEY uk_emp (tenant_id, emp_no)
) COMMENT='员工';

CREATE TABLE t_product (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  name VARCHAR(128) NOT NULL, spec VARCHAR(64), material VARCHAR(64), category VARCHAR(64),
  PRIMARY KEY (id)
) COMMENT='钢材品规';

-- ERP 标准中间库(DWD)：销售明细（覆盖四客户字段）
CREATE TABLE dwd_sales_detail (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  order_no VARCHAR(64) NOT NULL, biz_date DATE NOT NULL,
  emp_id BIGINT NULL, dept_id BIGINT NULL, branch_id BIGINT NULL,
  biz_type VARCHAR(32) COMMENT '现货/直发/外调/期货/自营/临调',
  store VARCHAR(32) COMMENT '库别:院内/调拨/直发/配货/加工/新品种',
  category VARCHAR(64), material VARCHAR(64), supplier VARCHAR(64),
  qty DECIMAL(16,3) COMMENT '数量/理算重量', unit_weight DECIMAL(16,4) COMMENT '单重',
  deal_price DECIMAL(16,4) COMMENT '成交单价', list_price DECIMAL(16,4) COMMENT '挂牌价',
  base_price DECIMAL(16,4) COMMENT '核定底价', cost_price DECIMAL(16,4) COMMENT '采购成本价',
  gross_profit DECIMAL(16,2) COMMENT '毛利/销售利润2', one_bill_cost DECIMAL(16,2) COMMENT '一票制成本',
  proc_fee DECIMAL(16,2) COMMENT '加工费支出', rebate DECIMAL(16,2) COMMENT '返利',
  other_inout DECIMAL(16,2) COMMENT '其他收支',
  settle_type VARCHAR(16) COMMENT '现款/欠款/垫资', credit_term INT COMMENT '约定回款账期(天)',
  is_internal TINYINT DEFAULT 0 COMMENT '内部销售(量算一半)', is_low_price TINYINT DEFAULT 0 COMMENT '低价备案',
  weigh_actual DECIMAL(16,2) COMMENT '实际加磅金额', weigh_std DECIMAL(16,2) COMMENT '标准加磅金额',
  PRIMARY KEY (id), KEY idx_sd (tenant_id, biz_date, emp_id)
) COMMENT='DWD销售明细';

-- 每日快照（日均/周转率/计息基础）
CREATE TABLE dwd_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  snap_date DATE NOT NULL, snap_type VARCHAR(24) NOT NULL COMMENT '库存/应收/预收/应付预付',
  subject_id BIGINT NULL, amount DECIMAL(18,2), qty DECIMAL(18,3),
  PRIMARY KEY (id), KEY idx_snap (tenant_id, snap_type, snap_date)
) COMMENT='每日快照';

-- 规则：绩效方案 / 核算项 / 多维作用域参数 / 阶梯 / 系数档 / 计息 / 变更单 / 版本
CREATE TABLE t_plan (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  name VARCHAR(128) NOT NULL, scope_json TEXT COMMENT '适用范围', period VARCHAR(16),
  version INT NOT NULL DEFAULT 1, status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  effective_from DATE NULL, PRIMARY KEY (id)
) COMMENT='绩效方案';

CREATE TABLE t_component (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL, plan_id BIGINT NOT NULL,
  name VARCHAR(128) NOT NULL, type VARCHAR(24) COMMENT '固定/公式/阶梯/系数/分成/计息/扣减/奖励',
  formula TEXT, dep_metrics VARCHAR(512), scope_json TEXT, enabled TINYINT DEFAULT 1,
  version INT NOT NULL DEFAULT 1, PRIMARY KEY (id)
) COMMENT='核算项';

CREATE TABLE t_scope_param (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  param_code VARCHAR(64) NOT NULL COMMENT '参数编码,如吨位单价',
  conditions_json VARCHAR(1024) COMMENT '维度条件JSON', param_value DECIMAL(18,6),
  source VARCHAR(128), version INT NOT NULL DEFAULT 1,
  PRIMARY KEY (id), KEY idx_sp (tenant_id, param_code)
) COMMENT='多维作用域参数';

CREATE TABLE t_tier_table (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  code VARCHAR(64) NOT NULL, mode VARCHAR(16) COMMENT 'WHOLE/PROGRESSIVE',
  rows_json TEXT COMMENT '[{lower,upper,factor}]', version INT NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
) COMMENT='阶梯表';

CREATE TABLE t_coefficient_table (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  code VARCHAR(64) NOT NULL, out_type VARCHAR(16) COMMENT 'COEF/AMOUNT',
  bands_json TEXT COMMENT '[{upper,value}]', version INT NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
) COMMENT='系数/金额档表';

CREATE TABLE t_interest_config (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  int_type VARCHAR(24) COMMENT '预收/应收/预付/应付/合同', direction VARCHAR(8) COMMENT '+/-',
  day_rate_bp DECIMAL(10,4), threshold DECIMAL(18,2), cap DECIMAL(18,2), enabled TINYINT DEFAULT 1,
  PRIMARY KEY (id)
) COMMENT='计息参数';

CREATE TABLE t_change_request (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  cr_no VARCHAR(64) NOT NULL, ver INT NOT NULL, submitter VARCHAR(64), submit_at DATETIME,
  eff_mode VARCHAR(16) COMMENT 'now/sched', eff_label VARCHAR(64), scope_label VARCHAR(64),
  status VARCHAR(16) COMMENT '待审批/待生效/已生效/已退回', cur_step INT DEFAULT 0,
  chain_json TEXT COMMENT '串签链(角色/姓名/或签/委托/加签/状态/时间)',
  details_json TEXT, params_json TEXT,
  PRIMARY KEY (id), UNIQUE KEY uk_cr (tenant_id, cr_no)
) COMMENT='规则变更单';

-- 结果：绩效单 / 提成核算单明细 / 审计
CREATE TABLE t_payslip (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  period VARCHAR(16) NOT NULL, emp_id BIGINT NOT NULL, plan_id BIGINT, plan_version INT,
  total_amount DECIMAL(16,2), status VARCHAR(16), snapshot_ref VARCHAR(128), calc_detail_json MEDIUMTEXT,
  PRIMARY KEY (id), KEY idx_ps (tenant_id, period, emp_id)
) COMMENT='个人绩效单';

CREATE TABLE t_commission_detail (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  period VARCHAR(16), emp_id BIGINT, order_no VARCHAR(64), item VARCHAR(64),
  amount DECIMAL(16,2), formula_snap VARCHAR(512),
  PRIMARY KEY (id), KEY idx_cd (tenant_id, period, emp_id)
) COMMENT='提成核算单明细';

CREATE TABLE t_audit_log (
  id BIGINT NOT NULL AUTO_INCREMENT, tenant_id VARCHAR(32) NOT NULL,
  op_user VARCHAR(64), op_type VARCHAR(64), target VARCHAR(128),
  before_val MEDIUMTEXT, after_val MEDIUMTEXT, ip VARCHAR(64), op_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id), KEY idx_audit (tenant_id, op_at)
) COMMENT='审计日志';
