<template>
  <div>
    <h2>规则变更审批</h2>
    <p class="muted">提交规则变更单（发布权重硬校验）→ 三级串签（主管→HR或签→分管领导）→ 生效；支持退回、灰度推广。</p>

    <el-card class="mb">
      <template #header>提交变更单</template>
      <el-form :inline="true">
        <el-form-item label="方案">
          <el-input v-model="form.planName" placeholder="销售现货方案" />
        </el-form-item>
        <el-form-item label="范围">
          <el-select v-model="form.scopeLabel" style="width: 180px">
            <el-option label="全公司" value="全公司" />
            <el-option label="灰度·超扑越销售部" value="灰度·超扑越销售部" />
          </el-select>
        </el-form-item>
        <el-form-item label="销售评分卡权重合计">
          <el-input-number v-model="salesWeight" :min="0" :max="200" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交（走审批）</el-button>
        </el-form-item>
      </el-form>
      <span v-if="error" class="err">{{ error }}</span>
    </el-card>

    <el-card>
      <template #header>
        变更单列表 <el-button size="small" text @click="load">刷新</el-button>
      </template>
      <el-table :data="list" size="small">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="chain">
              <el-steps :active="row.curStep" align-center finish-status="success" style="margin:10px 20px">
                <el-step v-for="(s, i) in row.chain" :key="i"
                  :title="s.role + ' · ' + s.name"
                  :description="stepDesc(s)" />
              </el-steps>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="单号" width="150" />
        <el-table-column label="版本" width="70"><template #default="{ row }">v{{ row.ver }}</template></el-table-column>
        <el-table-column prop="scopeLabel" label="范围" width="160" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="当前审批">
          <template #default="{ row }">
            <span v-if="row.status === 'PENDING' && row.chain[row.curStep]">
              {{ row.chain[row.curStep].role }} · {{ row.chain[row.curStep].name }}
              <el-tag v-if="row.chain[row.curStep].mode === 'OR_SIGN'" size="small" type="info">或签</el-tag>
            </span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="primary" @click="doApprove(row)">通过</el-button>
              <el-button size="small" @click="doReject(row)">退回</el-button>
            </template>
            <template v-else-if="row.status === 'SCHEDULED'">
              <el-button size="small" @click="doGoEff(row)">到期生效</el-button>
            </template>
            <el-button v-else-if="row.status === 'EFFECTIVE' && row.gray" size="small" type="warning" @click="doPromote(row)">推广全量</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import {
  submitCR, listCR, approveCR, rejectCR, promoteCR, goEffectiveCR, type ChangeRequest,
} from "../api/rule";

const form = ref({ planName: "销售现货方案", scopeLabel: "全公司" });
const salesWeight = ref(100);
const list = ref<ChangeRequest[]>([]);
const error = ref("");

function stepDesc(s: { mode: string; status: string; time: string | null; delegatedFrom: string | null; added: boolean }) {
  const parts: string[] = [];
  if (s.mode === "OR_SIGN") parts.push("或签");
  if (s.delegatedFrom) parts.push("受" + s.delegatedFrom + "委托");
  if (s.added) parts.push("加签");
  const st = { WAITING: "待处理", PASSED: "通过", REJECTED: "退回" }[s.status] || s.status;
  parts.push(st + (s.time ? " " + s.time : ""));
  return parts.join(" · ");
}

function statusText(s: string) {
  return { PENDING: "审批中", SCHEDULED: "待生效", EFFECTIVE: "已生效", REJECTED: "已退回" }[s] || s;
}
function statusType(s: string): "warning" | "primary" | "success" | "danger" | "info" {
  return ({ PENDING: "warning", SCHEDULED: "primary", EFFECTIVE: "success", REJECTED: "danger" } as const)[s] || "info";
}

async function load() {
  list.value = await listCR();
}

async function submit() {
  error.value = "";
  try {
    await submitCR({
      planName: form.value.planName,
      scopeLabel: form.value.scopeLabel,
      details: ["权重调整"],
      params: { 销售权重: "40/20/20/20" },
      scorecardWeightSums: { 销售评分卡: salesWeight.value, 采购评分卡: 100 },
    });
    ElMessage.success("已提交，进入三级串签");
    await load();
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : String(e);
  }
}

async function doApprove(row: ChangeRequest) {
  const approver = row.chain[row.curStep]?.name;
  await approveCR(row.id, approver);
  ElMessage.success("已通过");
  await load();
}
async function doReject(row: ChangeRequest) { await rejectCR(row.id); ElMessage.info("已退回"); await load(); }
async function doGoEff(row: ChangeRequest) { await goEffectiveCR(row.id); ElMessage.success("已生效"); await load(); }
async function doPromote(row: ChangeRequest) { await promoteCR(row.id); ElMessage.success("已发起全量推广"); await load(); }

onMounted(load);
</script>

<style scoped>
.muted { color: #888; } .mb { margin-bottom: 14px; } .err { color: #e0483d; margin-left: 10px; }
h2 { margin-bottom: 6px; }
</style>
