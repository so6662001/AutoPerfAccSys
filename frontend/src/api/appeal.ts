import http from "./http";

export interface Appeal {
  appealNo: string;
  period: string;
  empId: number;
  item: string;
  reason: string;
  status: string;
  verdict: string | null;
  submitter: string;
  reviewer: string | null;
  createdAt: string;
}

export const submitAppeal = (p: { period: string; empId: number; item: string; reason: string }): Promise<Appeal> =>
  http.post("/appeal", p);
export const listAppeal = (): Promise<Appeal[]> => http.get("/appeal");
export const adjudicate = (no: string, decision: string, note: string): Promise<Appeal> =>
  http.post(`/appeal/${no}/adjudicate`, { decision, note });
