package com.steel.perf.metric;

import com.steel.perf.common.exception.BizException;

import java.util.List;

/**
 * 指标计算服务（M1）。
 * 日均库存/应收、存货/应收周转率、挂价利润、业务净利润、成本重估、计息。
 * 纯计算，不依赖具体存储；数据由 perf-integration 从 ERP(SQLServer 只读)取数后传入。
 */
public class MetricService {

    /** 日均值 = 期间每日快照的算术平均。 */
    public double dailyAverage(List<Double> dailySnapshots) {
        if (dailySnapshots == null || dailySnapshots.isEmpty()) {
            throw new BizException("快照数据为空，无法计算日均值");
        }
        double sum = 0d;
        for (Double v : dailySnapshots) {
            sum += (v == null ? 0d : v);
        }
        return sum / dailySnapshots.size();
    }

    /** 周转率 = 分子(销量/销售额) / 日均值(日均库存/日均应收)。 */
    public double turnover(double numerator, double dailyAverage) {
        if (dailyAverage == 0d) {
            throw new BizException("日均值为0，无法计算周转率");
        }
        return numerator / dailyAverage;
    }

    /** 周转天数 = 考核期天数 / 周转率。 */
    public double turnoverDays(int periodDays, double turnover) {
        if (turnover == 0d) {
            throw new BizException("周转率为0，无法计算周转天数");
        }
        return periodDays / turnover;
    }

    /** 挂价利润 = (成交价 − 挂牌价) × 数量。 */
    public double listPriceProfit(double dealPrice, double listPrice, double qty) {
        return (dealPrice - listPrice) * qty;
    }

    /** 业务净利润 = 毛利 + 其他收入 − 其他支出。 */
    public double netProfit(double gross, double otherIncome, double otherExpense) {
        return gross + otherIncome - otherExpense;
    }

    /**
     * 成本重估·能力性收益 = 实际收益 − 市场自然波动收益。
     * 剔除行情普涨的账面利润，仅奖励跑赢市场的择时能力。
     */
    public double abilityIncome(double actualIncome, double marketNaturalIncome) {
        return actualIncome - marketNaturalIncome;
    }

    /** 逐日计息 = Σ(每日余额 × 日利率)。用于预收/应收/预付/应付。 */
    public double dailyBalanceInterest(List<Double> dailyBalances, double dayRate) {
        if (dailyBalances == null) {
            return 0d;
        }
        double total = 0d;
        for (Double b : dailyBalances) {
            total += (b == null ? 0d : b) * dayRate;
        }
        return total;
    }
}
