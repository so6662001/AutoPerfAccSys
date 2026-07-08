import http from "./http";

export interface AuditLog {
  id: number;
  tenantId: string;
  opUser: string;
  opType: string;
  target: string;
  detail: string;
  opAt: string;
}

export const listAudit = (): Promise<AuditLog[]> => http.get("/audit");
