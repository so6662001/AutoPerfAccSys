package com.steel.perf.engine.tier;

/**
 * 阶梯档位：区间 (lower, upper]，因子 factor（单价或比例）。
 * upper 为 null 表示 +∞（最高档）。
 */
public class TierRow {

    private final double lower;
    private final Double upper;
    private final double factor;

    public TierRow(double lower, Double upper, double factor) {
        this.lower = lower;
        this.upper = upper;
        this.factor = factor;
    }

    public boolean contains(double v) {
        boolean geLower = v > lower || lower == 0d && v >= 0d;
        boolean leUpper = upper == null || v <= upper;
        return geLower && leUpper;
    }

    public double getLower() {
        return lower;
    }

    public Double getUpper() {
        return upper;
    }

    public double getFactor() {
        return factor;
    }
}
