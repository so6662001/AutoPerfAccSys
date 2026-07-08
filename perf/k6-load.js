// k6 压测脚本：登录 → 核算 run（带幂等）+ 变更单查询。
// 运行：k6 run -e BASE=http://localhost:8080 perf/k6-load.js
import http from "k6/http";
import { check, sleep } from "k6";

const BASE = __ENV.BASE || "http://localhost:8080";

export const options = {
  scenarios: {
    ramp: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 50 },   // 爬坡到 50 并发
        { duration: "1m", target: 50 },    // 稳定压测
        { duration: "20s", target: 0 },    // 收尾
      ],
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],        // 错误率 < 1%
    http_req_duration: ["p(95)<500"],      // p95 < 500ms
  },
};

function login() {
  const res = http.post(`${BASE}/api/auth/login`, JSON.stringify({
    tenant: "demo", username: "loadtest", password: "perf@123", roles: ["HR"],
  }), { headers: { "Content-Type": "application/json" } });
  check(res, { "login 200": (r) => r.status === 200 });
  return JSON.parse(res.body).data.token;
}

const CALC_BODY = JSON.stringify({
  planCode: "XPY", version: 3, period: "2026-06",
  components: [
    { code: "基本工资", expression: "基数*系数", includeInTotal: true },
    { code: "吨位提成", expression: "现款量*现款单价+账期量*账期单价", includeInTotal: true },
    { code: "应收扣减", expression: "-吨位提成*扣比", includeInTotal: true },
  ],
  context: { 基数: 6000, 系数: 1, 现款量: 300, 现款单价: 8, 账期量: 500, 账期单价: 6, 扣比: 0.1 },
});

export default function () {
  const token = login();
  const headers = { "Content-Type": "application/json", Authorization: `Bearer ${token}` };

  const calc = http.post(`${BASE}/api/calc/run`, CALC_BODY, { headers });
  check(calc, {
    "calc 200": (r) => r.status === 200,
    "calc total=10860": (r) => JSON.parse(r.body).data.total === 10860,
  });

  const list = http.get(`${BASE}/api/rule/change-requests`, { headers });
  check(list, { "rule list 200": (r) => r.status === 200 });

  sleep(1);
}
