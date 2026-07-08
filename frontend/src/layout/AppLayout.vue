<template>
  <el-container class="app">
    <el-aside width="220px" class="aside">
      <div class="brand">钢绩云 · 绩效核算</div>
      <el-menu :default-active="active" router background-color="transparent" text-color="#c3ccdd" active-text-color="#fff">
        <el-menu-item index="/calc"><span>⚙ 核算工作台</span></el-menu-item>
        <el-menu-item index="/rule"><span>🧩 规则变更审批</span></el-menu-item>
        <el-menu-item index="/audit"><span>🔎 合规审计查询</span></el-menu-item>
        <el-menu-item index="/engine"><span>🧮 公式试算</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";
const route = useRoute();
const active = computed(() => route.path);
</script>

<style scoped>
.app { min-height: 100vh; }
.aside { background: linear-gradient(180deg, #0f1b30, #17233c); }
.brand { color: #fff; font-weight: 700; padding: 18px 16px; font-size: 15px; }
.main { background: #eef1f6; padding: 22px; }
:deep(.el-menu) { border-right: none; }
</style>
