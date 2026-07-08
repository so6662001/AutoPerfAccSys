import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import AppLayout from "../layout/AppLayout.vue";

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    component: AppLayout,
    redirect: "/calc",
    children: [
      { path: "calc", name: "calc", component: () => import("../views/CalcWorkbench.vue") },
      { path: "rule", name: "rule", component: () => import("../views/RuleCenter.vue") },
      { path: "audit", name: "audit", component: () => import("../views/AuditView.vue") },
      { path: "engine", name: "engine-demo", component: () => import("../views/EngineDemo.vue") },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫：M-后续接入 JWT 后在此校验登录与权限
router.beforeEach((_to, _from, next) => {
  next();
});

export default router;
