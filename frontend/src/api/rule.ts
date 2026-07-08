import http from "./http";

export interface ApprovalStep {
  role: string;
  name: string;
  mode: string;
  candidates: string[] | null;
  status: string;
  time: string | null;
  delegatedFrom: string | null;
  added: boolean;
}

export interface ChangeRequest {
  id: string;
  ver: number;
  submitter: string;
  effMode: string;
  effLabel: string;
  scopeLabel: string;
  status: string;
  curStep: number;
  chain: ApprovalStep[];
  details: string[] | null;
  params: Record<string, string> | null;
  gray: boolean;
}

export interface SubmitReq {
  planName: string;
  effMode?: string;
  effLabel?: string;
  scopeLabel?: string;
  details?: string[];
  params?: Record<string, string>;
  scorecardWeightSums?: Record<string, number>;
}

export const submitCR = (r: SubmitReq): Promise<ChangeRequest> => http.post("/rule/change-requests", r);
export const listCR = (): Promise<ChangeRequest[]> => http.get("/rule/change-requests");
export const approveCR = (id: string, approver: string): Promise<ChangeRequest> =>
  http.post(`/rule/change-requests/${id}/approve`, { approver });
export const rejectCR = (id: string): Promise<ChangeRequest> =>
  http.post(`/rule/change-requests/${id}/reject`, {});
export const promoteCR = (id: string): Promise<ChangeRequest> =>
  http.post(`/rule/change-requests/${id}/promote`, {});
export const goEffectiveCR = (id: string): Promise<ChangeRequest> =>
  http.post(`/rule/change-requests/${id}/go-effective`, {});
