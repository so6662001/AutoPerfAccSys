<template>
  <div>
    <h2>指标 · 计息试算</h2>
    <p class="muted">调用后端计息引擎：期货合同滚动计息（定金/货物分笔）与逐日计息（预收/应收/预付/应付）。</p>

    <el-card class="mb">
      <template #header>期货合同滚动计息（金额 × 天数 × 日利率）</template>
      <el-table :data="segments" size="small">
        <el-table-column label="金额(元)">
          <template #default="{ row }"><el-input-number v-model="row.amount" :step="10000" size="small" /></template>
        </el-table-column>
        <el-table-column label="天数">
          <template #default="{ row }"><el-input-number v-model="row.days" :min="0" size="small" /></template>
        </el-table-column>
        <el-table-column width="70">
          <template #default="{ $index }"><el-button size="small" text @click="segments.splice($index, 1)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <div class="mt">
        <el-button size="small" @click="segments.push({ amount: 100000, days: 6 })">+ 分笔</el-button>
        日利率(万分之)：<el-input-number v-model="bp" :min="0" :step="1" size="small" style="width:120px" />
        <el-button type="primary" size="small" :loading="loading" @click="calc">计算利息</el-button>
        <el-tag v-if="result !== null" type="success" style="margin-left:10px">利息 ¥{{ result.toLocaleString() }}</el-tag>
        <span v-if="error" class="err">{{ error }}</span>
      </div>
    </el-card>
    <el-alert type="info" :closable="false" title="示例：定金20万×4天+10万×6天，货物20万×6天+30万×10天+20万×10天，日利率万5 → 利息 ¥3,800" />
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { contractInterest, type Segment } from "../api/metric";

const segments = ref<Segment[]>([
  { amount: 200000, days: 4 }, { amount: 100000, days: 6 },
  { amount: 200000, days: 6 }, { amount: 300000, days: 10 }, { amount: 200000, days: 10 },
]);
const bp = ref(5);
const result = ref<number | null>(null);
const error = ref("");
const loading = ref(false);

async function calc() {
  error.value = "";
  loading.value = true;
  try {
    result.value = await contractInterest(segments.value, bp.value / 10000);
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.muted { color: #888; margin-bottom: 12px; } .mb { margin-bottom: 14px; } .mt { margin-top: 12px; }
.err { color: #e0483d; margin-left: 10px; } h2 { margin-bottom: 6px; }
</style>
