import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";

const routes: RouteRecordRaw[] = [
  { path: "/", redirect: "/engine" },
  {
    path: "/engine",
    name: "engine-demo",
    component: () => import("../views/EngineDemo.vue"),
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫：M1 接入 JWT 后在此校验登录与权限
router.beforeEach((_to, _from, next) => {
  next();
});

export default router;
