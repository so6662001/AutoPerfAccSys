package com.steel.perf.engine.incentive;

/**
 * 激励专项（P2）：新客户开发、累计吨量、按个计提、加工费提成。
 * 覆盖 岳洋通(开发新户前3月固定 + 外地新户累计吨量) 与 超扑越(地推纯新客户按个)。
 */
public class IncentiveService {

    /**
     * 新户按月固定奖励：合格(月提货达标)且在录入档案后前 N 个月内，每月固定奖励。
     *
     * @param monthsSinceEnroll 自录入档案起第几个月（1,2,3…）
     * @param withinMonths      有效月数（如 3）
     * @param monthlyReward     每月奖励（如 50 元）
     * @param qualified         是否达标（月提货 ≥ 阈值 且 经理审核）
     */
    public double newCustomerMonthlyBonus(int monthsSinceEnroll, int withinMonths,
                                          double monthlyReward, boolean qualified) {
        return (qualified && monthsSinceEnroll >= 1 && monthsSinceEnroll <= withinMonths) ? monthlyReward : 0d;
    }

    /** 累计吨量阶段奖励：累计提货量达阈值后，按吨计提（外地新户 6 月累计≥300吨 → 1元/吨）。 */
    public double cumulativeTonnageBonus(double cumulativeTons, double threshold, double perTonRate) {
        return cumulativeTons >= threshold ? cumulativeTons * perTonRate : 0d;
    }

    /** 按个计提（地推纯新客户 10 元/个）。 */
    public double perHeadReward(int count, double perHead) {
        return Math.max(count, 0) * perHead;
    }

    /** 加工费提成（加工费支出 × 比例，岳洋通 3%）。 */
    public double processingFeeCommission(double processingFee, double rate) {
        return processingFee * rate;
    }
}
