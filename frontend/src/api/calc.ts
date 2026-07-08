import http from "./http";

export interface ComponentReq {
  code: string;
  expression: string;
  includeInTotal: boolean;
}

export interface RunReq {
  planCode: string;
  version: number;
  components: ComponentReq[];
  summaryExpr?: string | null;
  context: Record<string, number>;
  period: string;
  snapshotHash?: string;
}

export interface ComponentTrace {
  code: string;
  expression: string;
  usedVars: Record<string, number>;
  amount: number;
}

export interface PayslipResult {
  planCode: string;
  planVersion: number;
  componentAmounts: Record<string, number>;
  traces: ComponentTrace[];
  total: number;
  idempotencyKey: string;
}

export function runCalc(req: RunReq): Promise<PayslipResult> {
  return http.post("/calc/run", req);
}
