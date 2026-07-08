<template>
  <div>
    <h2>合规审计查询</h2>
    <p class="muted">规则变更/审批等敏感操作全量留痕（租户隔离），按时间倒序。</p>
    <el-card>
      <template #header><el-button size="small" text @click="load">刷新</el-button></template>
      <el-table :data="rows" size="small">
        <el-table-column prop="opAt" label="时间" width="200" />
        <el-table-column prop="opUser" label="操作人" width="140" />
        <el-table-column label="操作类型" width="140">
          <template #default="{ row }"><el-tag>{{ row.opType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="target" label="对象" width="180" />
        <el-table-column prop="detail" label="明细" />
      </el-table>
      <el-empty v-if="!rows.length" description="暂无审计记录" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { listAudit, type AuditLog } from "../api/audit";

const rows = ref<AuditLog[]>([]);
async function load() {
  rows.value = await listAudit();
}
onMounted(load);
</script>

<style scoped>
.muted { color: #888; margin-bottom: 12px; } h2 { margin-bottom: 6px; }
</style>
