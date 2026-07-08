<template>
  <div>
    <h2>核算工作台</h2>
    <p class="muted">配置核算项与上下文变量，调用后端核算引擎，返回带逐项下钻的绩效单。默认示例复现超扑越绩效单 ¥20,835。</p>

    <el-card class="mb">
      <template #header>核算项（可引用指标/参数/其他核算项）</template>
      <el-table :data="components" size="small">
        <el-table-column label="编码" width="140">
          <template #default="{ row }"><el-input v-model="row.code" size="small" /></template>
        </el-table-column>
        <el-table-column label="公式">
          <template #default="{ row }"><el-input v-model="row.expression" size="small" /></template>
        </el-table-column>
        <el-table-column label="计入合计" width="90">
          <template #default="{ row }"><el-switch v-model="row.includeInTotal" /></template>
        </el-table-column>
        <el-table-column width="70">
          <template #default="{ $index }"><el-button size="small" text @click="components.splice($index, 1)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <el-button size="small" class="mt" @click="components.push({ code: '', expression: '', includeInTotal: true })">+ 核算项</el-button>
    </el-card>

    <el-card class="mb">
      <template #header>上下文变量 (JSON)</template>
      <el-input v-model="contextText" type="textarea" :rows="4" />
    </el-card>

    <el-button type="primary" :loading="loading" @click="run">执行核算</el-button>
    <span v-if="error" class="err">{{ error }}</span>

    <el-card v-if="result" class="mt">
      <template #header>
        绩效单 · {{ result.planCode }} v{{ result.planVersion }}
        <el-tag type="success" style="margin-left:8px">合计 ¥{{ result.total.toLocaleString() }}</el-tag>
        <span class="muted" style="margin-left:8px">幂等键 {{ result.idempotencyKey.slice(0, 12) }}…</span>
      </template>
      <el-table :data="result.traces" size="small">
        <el-table-column prop="code" label="核算项" width="160" />
        <el-table-column prop="expression" label="公式" />
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }"><b>¥{{ row.amount.toLocaleString() }}</b></template>
        </el-table-column>
        <el-table-column label="引用变量(下钻)" width="260">
          <template #default="{ row }">
            <span class="muted small">{{ Object.entries(row.usedVars).map(([k, v]) => `${k}=${v}`).join(', ') }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { runCalc, type ComponentReq, type PayslipResult } from "../api/calc";

const components = ref<ComponentReq[]>([
  { code: "基本工资", expression: "基数*系数", includeInTotal: true },
  { code: "吨位提成", expression: "现款量*现款单价+账期量*账期单价", includeInTotal: true },
  { code: "挂价所得", expression: "(成交价-挂牌价)*吨量*分配", includeInTotal: true },
  { code: "预收计息", expression: "预收*日利率*天数*分配", includeInTotal: true },
  { code: "应收扣减", expression: "-吨位提成*扣比", includeInTotal: true },
]);

const contextText = ref(JSON.stringify({
  基数: 6000, 系数: 1.0, 现款量: 300, 现款单价: 8, 账期量: 500, 账期单价: 6,
  成交价: 4010, 挂牌价: 4000, 吨量: 800, 分配: 0.7,
  预收: 500000, 日利率: 0.0005, 天数: 25, 扣比: 0.1,
}, null, 2));

const result = ref<PayslipResult | null>(null);
const error = ref("");
const loading = ref(false);

async function run() {
  error.value = "";
  loading.value = true;
  try {
    const context = JSON.parse(contextText.value) as Record<string, number>;
    result.value = await runCalc({
      planCode: "XPY-XIANHUO", version: 3, period: "2026-06",
      components: components.value, context,
    });
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.muted { color: #888; } .small { font-size: 12px; }
.mb { margin-bottom: 14px; } .mt { margin-top: 14px; }
.err { margin-left: 14px; color: #e0483d; }
h2 { margin-bottom: 6px; }
</style>
