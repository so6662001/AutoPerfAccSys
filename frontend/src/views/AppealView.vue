<template>
  <div>
    <h2>绩效申诉复核</h2>
    <p class="muted">员工发起申诉 → 复核裁定（调整重算 / 维持原判 / 升级复核），全程留痕。</p>

    <el-card class="mb">
      <template #header>发起申诉</template>
      <el-form :inline="true">
        <el-form-item label="周期"><el-input v-model="form.period" style="width:120px" /></el-form-item>
        <el-form-item label="员工ID"><el-input-number v-model="form.empId" :min="1" /></el-form-item>
        <el-form-item label="争议项"><el-input v-model="form.item" style="width:140px" /></el-form-item>
        <el-form-item label="理由"><el-input v-model="form.reason" style="width:220px" /></el-form-item>
        <el-form-item><el-button type="primary" @click="submit">提交申诉</el-button></el-form-item>
      </el-form>
      <span v-if="error" class="err">{{ error }}</span>
    </el-card>

    <el-card>
      <template #header>申诉工单 <el-button size="small" text @click="load">刷新</el-button></template>
      <el-table :data="rows" size="small">
        <el-table-column prop="appealNo" label="单号" width="150" />
        <el-table-column prop="item" label="争议项" width="120" />
        <el-table-column prop="reason" label="理由" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag :type="type(row.status)">{{ text(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="裁定" width="280">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="primary" @click="judge(row, 'ADJUST')">调整重算</el-button>
              <el-button size="small" @click="judge(row, 'KEEP')">维持</el-button>
              <el-button size="small" @click="judge(row, 'ESCALATE')">升级</el-button>
            </template>
            <span v-else class="muted">{{ row.verdict }}（{{ row.reviewer }}）</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { submitAppeal, listAppeal, adjudicate, type Appeal } from "../api/appeal";

const form = ref({ period: "2026-06", empId: 271, item: "挂价利润", reason: "成交价取数偏低" });
const rows = ref<Appeal[]>([]);
const error = ref("");

function text(s: string) {
  return { PENDING: "待复核", ADJUSTED: "已调整", KEPT: "已维持", ESCALATED: "已升级" }[s] || s;
}
function type(s: string): "warning" | "success" | "info" | "primary" {
  return ({ PENDING: "warning", ADJUSTED: "success", KEPT: "info", ESCALATED: "primary" } as const)[s] || "info";
}

async function load() { rows.value = await listAppeal(); }
async function submit() {
  error.value = "";
  try { await submitAppeal(form.value); ElMessage.success("已提交申诉"); await load(); }
  catch (e: unknown) { error.value = e instanceof Error ? e.message : String(e); }
}
async function judge(row: Appeal, decision: string) {
  await adjudicate(row.appealNo, decision, decision === "ADJUST" ? "修正取数并重算" : decision === "KEEP" ? "维持原判" : "升级上级复核");
  ElMessage.success("已裁定");
  await load();
}
onMounted(load);
</script>

<style scoped>
.muted { color: #888; } .mb { margin-bottom: 14px; } .err { color: #e0483d; } h2 { margin-bottom: 6px; }
</style>
