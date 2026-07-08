<template>
  <div>
    <h2>绩效明细表</h2>
    <p class="muted">逐单提成明细，按周期 + 日期区间筛选（不跨月）。</p>
    <el-card>
      <template #header>
        周期 <el-input v-model="period" style="width:120px" />
        日 <el-input-number v-model="from" :min="1" :max="31" size="small" /> –
        <el-input-number v-model="to" :min="1" :max="31" size="small" />
        <el-button size="small" type="primary" @click="load">查询</el-button>
        <el-tag style="margin-left:10px">合计 ¥{{ sum.toLocaleString() }}</el-tag>
        <span v-if="error" class="err">{{ error }}</span>
      </template>
      <el-table :data="rows" size="small">
        <el-table-column prop="bizDate" label="日期" width="120" />
        <el-table-column prop="orderNo" label="单号" width="120" />
        <el-table-column prop="item" label="提成项" width="140" />
        <el-table-column label="金额" align="right">
          <template #default="{ row }"><b>¥{{ row.amount.toLocaleString() }}</b></template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!rows.length" description="无明细" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import http from "../api/http";

interface Detail { bizDate: string; orderNo: string; item: string; amount: number; }
const period = ref("2026-06");
const from = ref(1);
const to = ref(31);
const rows = ref<Detail[]>([]);
const error = ref("");
const sum = computed(() => rows.value.reduce((s, r) => s + r.amount, 0));

async function load() {
  error.value = "";
  try {
    rows.value = await http.get<unknown, Detail[]>("/commission/details", {
      params: { period: period.value, from: from.value, to: to.value },
    });
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : String(e);
  }
}
onMounted(load);
</script>

<style scoped>
.muted { color: #888; } .err { color: #e0483d; margin-left: 10px; } h2 { margin-bottom: 6px; }
</style>
