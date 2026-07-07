package com.steel.perf.engine.tier;

import com.steel.perf.common.exception.BizException;

import java.util.List;

/**
 * 阶梯 / 门槛 / 阶跃 / 封顶 引擎（平台基础能力 P0）。
 * <ul>
 *   <li>整体档位 WHOLE：value × 所在档因子（名亿"完成900→第三行×单价"、超扑越淡旺季）</li>
 *   <li>累进 PROGRESSIVE：各档区间量 × 各档因子求和</li>
 *   <li>门槛 threshold：低于门槛不计（信桥 −20 / 岳洋通 ≥10）</li>
 *   <li>阶跃 stair：(value − threshold)/step（岳洋通 (差价−10)/10）</li>
 *   <li>封顶 cap：min(value, upper)（信桥 吨利润 ≤ 50）</li>
 * </ul>
 */
public class TierEngine {

    public enum Mode { WHOLE, PROGRESSIVE }

    /** 阶梯计算：base 为计量值（吨量/收益）。 */
    public double step(double base, List<TierRow> rows, Mode mode) {
        if (rows == null || rows.isEmpty()) {
            throw new BizException("阶梯表为空");
        }
        if (mode == Mode.WHOLE) {
            for (TierRow r : rows) {
                if (r.contains(base)) {
                    return base * r.getFactor();
                }
            }
            // 超出所有档，取最高档
            return base * rows.get(rows.size() - 1).getFactor();
        }
        // PROGRESSIVE：逐档累进
        double total = 0d;
        for (TierRow r : rows) {
            double lo = r.getLower();
            double hi = r.getUpper() == null ? Double.MAX_VALUE : r.getUpper();
            if (base > lo) {
                double portion = Math.min(base, hi) - lo;
                if (portion > 0) {
                    total += portion * r.getFactor();
                }
            }
        }
        return total;
    }

    /** 门槛：value 达到门槛才保留，否则返回 0。 */
    public double threshold(double value, double threshold) {
        return value >= threshold ? value : 0d;
    }

    /** 阶跃：(value − threshold)/step，低于门槛返回 0。用于 (差价−10)/10 之类。 */
    public double stair(double value, double threshold, double step) {
        if (step <= 0) {
            throw new BizException("阶跃步长必须为正");
        }
        if (value < threshold) {
            return 0d;
        }
        return (value - threshold) / step;
    }

    /** 单位封顶：min(value, upper)。 */
    public double cap(double value, double upper) {
        return Math.min(value, upper);
    }
}
