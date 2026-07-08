package com.steel.perf.metric;

import java.util.List;

/**
 * 期货合同滚动计息（岳洋通专项）。
 * 定金按到账分笔计息、货物按入库到出货计息，均以"金额 × 天数"分笔累加，
 * 受合同总额封顶约束（封顶在上游拆分分笔时体现，此处按已拆分分笔计息）。
 */
public class ContractInterestService {

    /** 计息分笔：金额 × 天数。 */
    public record Segment(double amount, int days, String note) {
        public Segment(double amount, int days) {
            this(amount, days, null);
        }
    }

    /** 利息 = Σ(金额 × 天数) × 日利率。 */
    public double interest(List<Segment> segments, double dayRate) {
        double amountDays = amountDays(segments);
        return amountDays * dayRate;
    }

    /** 计息基数合计（元·天）。 */
    public double amountDays(List<Segment> segments) {
        if (segments == null) {
            return 0d;
        }
        double total = 0d;
        for (Segment s : segments) {
            total += s.amount() * s.days();
        }
        return total;
    }
}
