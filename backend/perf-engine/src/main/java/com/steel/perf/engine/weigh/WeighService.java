package com.steel.perf.engine.weigh;

/**
 * 过磅业务能力（超扑越专项 P2）。
 * <ul>
 *   <li>加磅利润提成 = 实际加磅销售金额 − 按标准加磅千分比计算的销售金额（非负）。</li>
 *   <li>点数自营利润 = 实际成交价 − (出货时同规格最高挂价 − 30) × 单重/1000；定价做 0.5 进位处理。</li>
 * </ul>
 */
public class WeighService {

    /** 加磅利润提成（仅过磅，非负）。 */
    public double weighProfit(double actualWeighAmount, double stdWeighAmount) {
        return Math.max(actualWeighAmount - stdWeighAmount, 0d);
    }

    /**
     * 0.5 进位处理：不满 0.5 凑满 0.5，满 0.5 进位（向上取整到 0.5 的倍数）。
     * 例：3.2→3.5，3.6→4.0，3.5→3.5，3.0→3.0。
     */
    public double halfRoundUp(double v) {
        return Math.ceil(v * 2d) / 2d;
    }

    /**
     * 点数销售自营利润。
     * 定价 = (同规格最高挂价 − 30) × 单重/1000，经 0.5 进位；自营利润 = 成交价 − 定价。
     */
    public double pointBasedSelfProfit(double dealPrice, double maxListPriceSameSpec, double unitWeight) {
        double basePrice = (maxListPriceSameSpec - 30d) * unitWeight / 1000d;
        double rounded = halfRoundUp(basePrice);
        return dealPrice - rounded;
    }
}
