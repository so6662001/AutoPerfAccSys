package com.steel.perf.engine.cost;

/**
 * 成本口径引擎（平台基础能力 P0）。
 * 覆盖：名亿"结算方式驱动成本"（现款/欠款/垫资），信桥"一票制成本"，
 * 以及"毛利 − 返利/其他收支"调整。
 */
public class CostEngine {

    public enum SettlementMode { CASH, CREDIT, ADVANCE }

    /**
     * 结算方式驱动成本（名亿）：
     * 现款 -> 销量 × cashPerTon；欠款 -> 0；垫资 -> advanceFixed（固定）。
     */
    public double settlementCost(double qty, SettlementMode mode, double cashPerTon, double advanceFixed) {
        return switch (mode) {
            case CASH -> qty * cashPerTon;
            case CREDIT -> 0d;
            case ADVANCE -> advanceFixed;
        };
    }

    /** 毛利调整：毛利 − 返利 − 其他收支（名亿/超扑越）。 */
    public double adjustedGross(double gross, double rebate, double otherInOut) {
        return gross - rebate - otherInOut;
    }
}
