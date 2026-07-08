package com.steel.perf.metric;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContractInterestServiceTest {

    private final ContractInterestService svc = new ContractInterestService();

    @Test
    void futuresRollingInterestMatchesExample() {
        // 岳洋通示例末次状态：
        // 定金计息 20万×4天 + 10万×6天；货物计息 20万×6天 + 30万×10天 + 20万×10天
        List<ContractInterestService.Segment> segs = List.of(
                new ContractInterestService.Segment(200000d, 4),
                new ContractInterestService.Segment(100000d, 6),
                new ContractInterestService.Segment(200000d, 6),
                new ContractInterestService.Segment(300000d, 10),
                new ContractInterestService.Segment(200000d, 10)
        );
        assertEquals(7_600_000d, svc.amountDays(segs), 1e-6);        // 元·天
        assertEquals(3800d, svc.interest(segs, 0.0005d), 1e-6);     // ×万5 = 3800
    }
}
