<template>
  <div>
    <h2>消息中心</h2>
    <p class="muted">规则生效等通知（可点击追溯变更单）。</p>
    <el-card>
      <template #header><el-button size="small" text @click="load">刷新</el-button></template>
      <div v-for="n in rows" :key="n.id" class="notice">
        <div class="t">🔔 {{ n.title }}</div>
        <div class="b">{{ n.body }}</div>
        <div class="m">{{ n.createdAt }} <el-tag v-if="n.crId" size="small">{{ n.crId }}</el-tag></div>
      </div>
      <el-empty v-if="!rows.length" description="暂无消息" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import http from "../api/http";

interface Notice { id: number; title: string; body: string; crId: string | null; createdAt: string; }
const rows = ref<Notice[]>([]);
async function load() { rows.value = await http.get<unknown, Notice[]>("/notice"); }
onMounted(load);
</script>

<style scoped>
.muted { color: #888; margin-bottom: 12px; } h2 { margin-bottom: 6px; }
.notice { border-left: 3px solid #2f5bea; padding: 8px 12px; margin-bottom: 10px; background: #f7f9fc; border-radius: 6px; }
.notice .t { font-weight: 700; font-size: 14px; } .notice .b { color: #5a6577; font-size: 13px; margin: 4px 0; }
.notice .m { color: #8a94a6; font-size: 12px; }
</style>
