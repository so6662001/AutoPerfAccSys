<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <div class="brand">钢绩云 · 绩效核算系统</div>
      <el-form @submit.prevent>
        <el-form-item>
          <el-input v-model="tenant" placeholder="租户 (如 demo)" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="username" placeholder="用户名" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="password" type="password" placeholder="密码 (演示 perf@123)" show-password @keyup.enter="doLogin" />
        </el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="doLogin">登录</el-button>
        <div v-if="error" class="err">{{ error }}</div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { login } from "../api/auth";

const tenant = ref("demo");
const username = ref("admin");
const password = ref("perf@123");
const error = ref("");
const loading = ref(false);
const router = useRouter();

async function doLogin() {
  error.value = "";
  loading.value = true;
  try {
    await login(tenant.value, username.value, password.value);
    router.push("/calc");
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : String(e);
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-wrap { min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1c2c50, #0f1b30); }
.login-card { width: 360px; }
.brand { font-size: 18px; font-weight: 700; text-align: center; margin-bottom: 18px; color: #1b2738; }
.err { color: #e0483d; margin-top: 10px; font-size: 13px; }
</style>
