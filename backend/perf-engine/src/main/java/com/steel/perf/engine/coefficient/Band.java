package com.steel.perf.engine.coefficient;

/**
 * 区间映射档：达成率 ≤ upper 时取 value（value 可为系数或固定金额）。
 * upper 为 null 表示 +∞（最高档）。
 */
public class Band {

    private final Double upper;
    private final double value;

    public Band(Double upper, double value) {
        this.upper = upper;
        this.value = value;
    }

    public Double getUpper() {
        return upper;
    }

    public double getValue() {
        return value;
    }
}
