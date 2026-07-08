import axios from "axios";

const http = axios.create({
  baseURL: "/api",
  timeout: 15000,
});

// 请求拦截：注入租户/鉴权头（M1 用 JWT 替换）
http.interceptors.request.use((config) => {
  config.headers["X-Tenant-Id"] = localStorage.getItem("tenantId") || "demo";
  config.headers["X-User-Id"] = localStorage.getItem("userId") || "u-admin";
  const token = localStorage.getItem("token");
  if (token) config.headers["Authorization"] = `Bearer ${token}`;
  return config;
});

// 响应拦截：统一返回体拆包
http.interceptors.response.use(
  (resp) => {
    const body = resp.data;
    if (body && typeof body.code === "number" && body.code !== 0) {
      return Promise.reject(new Error(body.message || "请求失败"));
    }
    return body?.data ?? body;
  },
  (err) => Promise.reject(err),
);

export default http;
