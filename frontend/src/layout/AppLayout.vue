<template>
  <el-container class="app">
    <el-aside width="220px" class="aside">
      <div class="brand">钢绩云 · 绩效核算</div>
      <el-menu :default-active="active" router background-color="transparent" text-color="#c3ccdd" active-text-color="#fff">
        <el-menu-item index="/calc"><span>⚙ 核算工作台</span></el-menu-item>
        <el-menu-item index="/payslip"><span>🧾 绩效单查询</span></el-menu-item>
        <el-menu-item index="/commission"><span>📋 绩效明细表</span></el-menu-item>
        <el-menu-item index="/rule"><span>🧩 规则变更审批</span></el-menu-item>
        <el-menu-item index="/appeal"><span>⚖ 绩效申诉复核</span></el-menu-item>
        <el-menu-item index="/sandbox"><span>⚗ 政策沙盘对比</span></el-menu-item>
        <el-menu-item index="/metric"><span>💹 指标计息试算</span></el-menu-item>
        <el-menu-item index="/audit"><span>🔎 合规审计查询</span></el-menu-item>
        <el-menu-item index="/engine"><span>🧮 公式试算</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <span class="sp"></span>
        <span class="user">{{ user.username }} @ {{ user.tenant }}</span>
        <el-button size="small" text @click="onLogout">退出</el-button>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { currentUser, logout } from "../api/auth";
const route = useRoute();
const router = useRouter();
const active = computed(() => route.path);
const user = currentUser();
function onLogout() {
  logout();
  router.push("/login");
}
</script>

<style scoped>
.app { min-height: 100vh; }
.aside { background: linear-gradient(180deg, #0f1b30, #17233c); }
.brand { color: #fff; font-weight: 700; padding: 18px 16px; font-size: 15px; }
.main { background: #eef1f6; padding: 22px; }
.topbar { background: #fff; border-bottom: 1px solid #e3e8f0; display: flex; align-items: center; height: 52px; }
.topbar .sp { flex: 1; } .topbar .user { color: #5a6577; font-size: 13px; margin-right: 12px; }
:deep(.el-menu) { border-right: none; }
</style>
