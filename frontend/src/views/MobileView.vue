<template>
  <div class="m-wrap">
    <div class="m-head">
      <div class="r">
        <div class="av">{{ (user.username || '员')[0] }}</div>
        <div>
          <div class="nm">{{ user.username || '员工' }}</div>
          <div class="sub">{{ user.tenant }} · 绩效</div>
        </div>
        <span class="sp"></span>
        <span class="logout" @click="onLogout">退出</span>
      </div>
      <div class="amount">
        <div class="l">本期薪酬合计（{{ period }}）</div>
        <div class="v">¥{{ total.toLocaleString() }}</div>
      </div>
    </div>

    <div class="m-body">
      <div class="m-title">消息中心 <span v-if="notices.length" class="badge">{{ notices.length }}</span></div>
      <div v-for="n in notices" :key="n.id" class="m-card notice">
        <div class="t">🔔 {{ n.title }}</div>
        <div class="b">{{ n.body }}</div>
      </div>
      <el-empty v-if="!notices.length" description="暂无消息" :image-size="60" />

      <div class="m-title">我的绩效单</div>
      <div v-for="p in payslips" :key="p.idempotencyKey" class="m-card">
        <div class="row"><span>{{ p.planCode }} v{{ p.planVersion }}</span><b>¥{{ p.total.toLocaleString() }}</b></div>
        <div class="detail">
          <div v-for="(t, i) in parse(p.detailJson)" :key="i" class="dl">
            <span>{{ t.code }}</span><span>¥{{ t.amount.toLocaleString() }}</span>
          </div>
        </div>
      </div>
      <el-empty v-if="!payslips.length" description="暂无绩效单" :image-size="60" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import http from "../api/http";
import { currentUser, logout } from "../api/auth";

interface Payslip { planCode: string; planVersion: number; total: number; idempotencyKey: string; detailJson: string; }
interface Notice { id: number; title: string; body: string; }
interface Trace { code: string; amount: number; }

const user = currentUser();
const period = ref("2026-06");
const payslips = ref<Payslip[]>([]);
const notices = ref<Notice[]>([]);
const router = useRouter();
const total = computed(() => payslips.value.reduce((s, p) => s + p.total, 0));

function parse(json: string): Trace[] { try { return json ? JSON.parse(json) : []; } catch { return []; } }
function onLogout() { logout(); router.push("/login"); }

onMounted(async () => {
  payslips.value = await http.get<unknown, Payslip[]>("/calc/payslips", { params: { period: period.value } });
  notices.value = await http.get<unknown, Notice[]>("/notice");
});
</script>

<style scoped>
.m-wrap { max-width: 480px; margin: 0 auto; min-height: 100vh; background: #f4f6fa; }
.m-head { background: linear-gradient(135deg, #2f5bea, #2244c4); color: #fff; padding: 18px 16px 22px; }
.m-head .r { display: flex; align-items: center; gap: 10px; }
.m-head .av { width: 40px; height: 40px; border-radius: 50%; background: rgba(255,255,255,.2); display: grid; place-items: center; font-weight: 700; }
.m-head .nm { font-weight: 700; } .m-head .sub { font-size: 12px; opacity: .85; }
.m-head .sp { flex: 1; } .m-head .logout { font-size: 12px; opacity: .9; }
.amount { margin-top: 16px; } .amount .l { font-size: 12px; opacity: .85; } .amount .v { font-size: 30px; font-weight: 800; }
.m-body { padding: 14px; }
.m-title { font-weight: 700; font-size: 13px; color: #5a6577; margin: 10px 2px; }
.badge { background: #e0483d; color: #fff; font-size: 10px; padding: 1px 6px; border-radius: 10px; }
.m-card { background: #fff; border-radius: 12px; padding: 12px 14px; margin-bottom: 10px; box-shadow: 0 1px 4px rgba(16,32,64,.06); }
.m-card.notice { border-left: 3px solid #2f5bea; }
.notice .t { font-weight: 700; font-size: 13px; } .notice .b { font-size: 12px; color: #5a6577; margin-top: 4px; }
.m-card .row { display: flex; justify-content: space-between; font-weight: 700; }
.detail { margin-top: 8px; } .dl { display: flex; justify-content: space-between; font-size: 12px; color: #5a6577; padding: 3px 0; border-top: 1px dashed #eef1f6; }
</style>
