<template>
  <div class="wrap">
    <h2>核算引擎联调 · 公式试算</h2>
    <p class="muted">调用后端 /api/engine/formula，验证公式引擎（沙箱）。</p>

    <el-form label-width="90px" style="max-width: 640px">
      <el-form-item label="公式">
        <el-input v-model="expression" placeholder="(deal-list)*qty*alloc" />
      </el-form-item>
      <el-form-item label="变量(JSON)">
        <el-input v-model="varsText" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="run">试算</el-button>
        <span v-if="result !== null" class="result">结果：{{ result }}</span>
        <span v-if="error" class="err">{{ error }}</span>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import http from "../api/http";

const expression = ref("(deal-list)*qty*alloc");
const varsText = ref('{"deal":4010,"list":4000,"qty":800,"alloc":0.7}');
const result = ref<number | null>(null);
const error = ref("");
const loading = ref(false);

async function run() {
  error.value = "";
  result.value = null;
  loading.value = true;
  try {
    const vars = JSON.parse(varsText.value);
    result.value = await http.post("/engine/formula", { expression: expression.value, vars });
  } catch (e: any) {
    error.value = e?.message || String(e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.wrap { padding: 28px; }
.muted { color: #888; margin-bottom: 16px; }
.result { margin-left: 16px; font-weight: 700; color: #2f5bea; }
.err { margin-left: 16px; color: #e0483d; }
</style>
