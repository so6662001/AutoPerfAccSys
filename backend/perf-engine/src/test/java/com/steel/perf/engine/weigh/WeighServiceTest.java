package com.steel.perf.engine.weigh;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeighServiceTest {

    private final WeighService svc = new WeighService();

    @Test
    void weighProfitNonNegative() {
        assertEquals(1300d, svc.weighProfit(8500d, 7200d), 1e-9);
        assertEquals(0d, svc.weighProfit(7000d, 7200d), 1e-9);
    }

    @Test
    void halfRoundUp() {
        assertEquals(3.5d, svc.halfRoundUp(3.2d), 1e-9); // 不满0.5凑满0.5
        assertEquals(4.0d, svc.halfRoundUp(3.6d), 1e-9); // 满0.5进位
        assertEquals(3.5d, svc.halfRoundUp(3.5d), 1e-9);
        assertEquals(3.0d, svc.halfRoundUp(3.0d), 1e-9);
    }

    @Test
    void pointBasedSelfProfit() {
        // 定价 = (4030-30)*1000/1000 = 4000 (整) -> 0.5进位仍4000; 自营利润 = 4010-4000 = 10
        assertEquals(10d, svc.pointBasedSelfProfit(4010d, 4030d, 1000d), 1e-9);
    }
}
