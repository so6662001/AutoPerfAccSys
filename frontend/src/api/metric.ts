import http from "./http";

export interface Segment {
  amount: number;
  days: number;
}

export const contractInterest = (segments: Segment[], dayRate: number): Promise<number> =>
  http.post("/metric/interest/contract", { segments, dayRate });

export const dailyInterest = (dailyBalances: number[], dayRate: number): Promise<number> =>
  http.post("/metric/interest/daily", { dailyBalances, dayRate });
