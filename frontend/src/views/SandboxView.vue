<template>
  <div>
    <h2>政策沙盘对比</h2>
    <p class="muted">同一方案、多套情景并排试算，对比方案效果（不落库）。默认：吨位提成单价 A=8 / B=10 / C=12。</p>

    <el-card class="mb">
      <template #header>核算项</template>
      <el-input v-model="expression" placeholder="量*单价" />
      <div class="muted small mt">公式变量将来自各情景 context。</div>
    </el-card>

    <el-card class="mb">
      <template #header>情景（各自变量 JSON）</template>
      <el-table :data="scenarios" size="small">
        <el-table-column label="名称" width="140">
          <template #default="{ row }"><el-input v-model="row.name" size="small" /></template>
        </el-table-column>
        <el-table-column label="变量(JSON)">
          <template #default="{ row }"><el-input v-model="row.ctx" size="small" /></template>
        </el-table-column>
      </el-table>
      <el-button size="small" class="mt" @click="addScenario">+ 情景</el-button>
      <el-button type="primary" size="small" class="mt" :loading="loading" @click="run">对比试算</el-button>
      <span v-if="error" class="err">{{ error }}</span>
    </el-card>

    <el-card v-if="results.length">
      <template #header>对比结果</template>
      <el-table :data="results" size="small">
        <el-table-column prop="name" label="情景" width="160" />
        <el-table-column label="合计">
          <template #default="{ row }">
            <b>¥{{ row.total.toLocaleString() }}</b>
            <el-tag v-if="row.total === maxTotal" type="success" size="small" style="margin-left:8px">最优</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import http from "../api/http";

interface ScenarioResult { name: string; total: number; componentAmounts: Record<string, number>; }

const expression = ref("量*单价");
const scenarios = ref([
  { name: "方案A", ctx: '{"量":100,"单价":8}' },
  { name: "方案B", ctx: '{"量":100,"单价":10}' },
  { name: "方案C", ctx: '{"量":100,"单价":12}' },
]);
const results = ref<ScenarioResult[]>([]);
const error = ref("");
const loading = ref(false);
const maxTotal = computed(() => Math.max(0, ...results.value.map((r) => r.total)));

function addScenario() {
  scenarios.value.push({ name: "方案X", ctx: JSON.stringify({ 量: 100, 单价: 6 }) });
}

async function run() {
  error.value = "";
  loading.value = true;
  try {
    const payload = {
      planCode: "SANDBOX", version: 1, period: "2026-06",
      components: [{ code: "吨位提成", expression: expression.value, includeInTotal: true }],
      scenarios: scenarios.value.map((s) => ({ name: s.name, context: JSON.parse(s.ctx) })),
    };
    results.value = await http.post("/calc/sandbox", payload);
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.muted { color: #888; } .small { font-size: 12px; } .mb { margin-bottom: 14px; } .mt { margin-top: 10px; }
.err { color: #e0483d; margin-left: 10px; } h2 { margin-bottom: 6px; }
</style>
