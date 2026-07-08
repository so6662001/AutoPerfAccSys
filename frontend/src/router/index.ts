import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import AppLayout from "../layout/AppLayout.vue";

const routes: RouteRecordRaw[] = [
  { path: "/login", name: "login", component: () => import("../views/LoginView.vue") },
  {
    path: "/",
    component: AppLayout,
    redirect: "/calc",
    children: [
      { path: "calc", name: "calc", component: () => import("../views/CalcWorkbench.vue") },
      { path: "rule", name: "rule", component: () => import("../views/RuleCenter.vue") },
      { path: "appeal", name: "appeal", component: () => import("../views/AppealView.vue") },
      { path: "sandbox", name: "sandbox", component: () => import("../views/SandboxView.vue") },
      { path: "metric", name: "metric", component: () => import("../views/MetricView.vue") },
      { path: "audit", name: "audit", component: () => import("../views/AuditView.vue") },
      { path: "engine", name: "engine-demo", component: () => import("../views/EngineDemo.vue") },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫：未登录跳转登录页
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem("token");
  if (to.path !== "/login" && !token) {
    next("/login");
  } else {
    next();
  }
});

export default router;
