<template>
  <div>
    <h2>绩效单查询</h2>
    <p class="muted">按周期查询已核算并落库的绩效单（幂等去重），展开查看逐项下钻明细。</p>
    <el-card>
      <template #header>
        周期：<el-input v-model="period" style="width:140px" />
        <el-button size="small" type="primary" @click="load">查询</el-button>
      </template>
      <el-table :data="rows" size="small">
        <el-table-column type="expand">
          <template #default="{ row }">
            <el-table :data="parseDetail(row.detailJson)" size="small" style="margin:8px 24px">
              <el-table-column prop="code" label="核算项" width="180" />
              <el-table-column prop="expression" label="公式" />
              <el-table-column label="金额" width="120" align="right">
                <template #default="{ row: r }">¥{{ r.amount.toLocaleString() }}</template>
              </el-table-column>
            </el-table>
          </template>
        </el-table-column>
        <el-table-column prop="planCode" label="方案" width="150" />
        <el-table-column label="版本" width="70"><template #default="{ row }">v{{ row.planVersion }}</template></el-table-column>
        <el-table-column label="合计"><template #default="{ row }"><b>¥{{ row.total.toLocaleString() }}</b></template></el-table-column>
        <el-table-column prop="idempotencyKey" label="幂等键" width="180">
          <template #default="{ row }"><span class="muted small">{{ row.idempotencyKey.slice(0, 16) }}…</span></template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!rows.length" description="无绩效单（可在核算工作台执行核算）" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import http from "../api/http";

interface Payslip { planCode: string; planVersion: number; total: number; idempotencyKey: string; detailJson: string; }
interface Trace { code: string; expression: string; amount: number; }

const period = ref("2026-06");
const rows = ref<Payslip[]>([]);

async function load() {
  rows.value = await http.get<unknown, Payslip[]>("/calc/payslips", { params: { period: period.value } });
}
function parseDetail(json: string): Trace[] {
  try { return json ? (JSON.parse(json) as Trace[]) : []; } catch { return []; }
}
onMounted(load);
</script>

<style scoped>
.muted { color: #888; } .small { font-size: 12px; } h2 { margin-bottom: 6px; }
</style>
