package com.steel.perf.engine.incentive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncentiveServiceTest {

    private final IncentiveService svc = new IncentiveService();

    @Test
    void newCustomerMonthlyBonus() {
        assertEquals(50d, svc.newCustomerMonthlyBonus(2, 3, 50d, true), 1e-9);   // 前3月内且达标
        assertEquals(0d, svc.newCustomerMonthlyBonus(4, 3, 50d, true), 1e-9);    // 超过3月
        assertEquals(0d, svc.newCustomerMonthlyBonus(1, 3, 50d, false), 1e-9);   // 未达标
    }

    @Test
    void cumulativeTonnageBonus() {
        assertEquals(320d, svc.cumulativeTonnageBonus(320d, 300d, 1d), 1e-9);    // ≥300 → 1元/吨
        assertEquals(0d, svc.cumulativeTonnageBonus(250d, 300d, 1d), 1e-9);      // 未达阈值
    }

    @Test
    void perHeadAndProcessingFee() {
        assertEquals(30d, svc.perHeadReward(3, 10d), 1e-9);                       // 地推纯新客户 10元/个
        assertEquals(240d, svc.processingFeeCommission(8000d, 0.03d), 1e-9);      // 加工费×3%
    }
}
