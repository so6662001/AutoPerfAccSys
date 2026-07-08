import http from "./http";

export interface LoginResult {
  token: string;
  tenant: string;
  username: string;
  roles: string[];
}

export async function login(tenant: string, username: string, password: string): Promise<LoginResult> {
  const r = await http.post<unknown, LoginResult>("/auth/login", { tenant, username, password });
  localStorage.setItem("token", r.token);
  localStorage.setItem("tenantId", r.tenant);
  localStorage.setItem("userId", r.username);
  localStorage.setItem("roles", JSON.stringify(r.roles));
  return r;
}

export function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("roles");
}

export function currentUser() {
  return {
    tenant: localStorage.getItem("tenantId") || "",
    username: localStorage.getItem("userId") || "",
    token: localStorage.getItem("token") || "",
  };
}
