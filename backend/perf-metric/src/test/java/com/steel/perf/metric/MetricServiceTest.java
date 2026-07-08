package com.steel.perf.metric;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricServiceTest {

    private final MetricService svc = new MetricService();

    @Test
    void dailyAverageAndTurnover() {
        double avg = svc.dailyAverage(List.of(100d, 120d, 80d, 100d)); // 400/4=100
        assertEquals(100d, avg, 1e-9);
        double turnover = svc.turnover(1200d, avg);                    // 1200/100 = 12
        assertEquals(12d, turnover, 1e-9);
        assertEquals(2.5d, svc.turnoverDays(30, turnover), 1e-9);      // 30/12 = 2.5
    }

    @Test
    void listPriceAndNetProfit() {
        assertEquals(8000d, svc.listPriceProfit(4010d, 4000d, 800d), 1e-9);
        assertEquals(24500d, svc.netProfit(24000d, 1000d, 500d), 1e-9);
    }

    @Test
    void abilityIncomeExcludesMarketRally() {
        // 实际收益 60万，市场自然波动 20万 -> 能力性收益 40万
        assertEquals(400000d, svc.abilityIncome(600000d, 200000d), 1e-9);
    }

    @Test
    void dailyBalanceInterest() {
        // 预收 50万 连续 25 天，日利率万5 -> 500000*0.0005*25 = 6250
        double rate = 0.0005d;
        double[] arr = new double[25];
        java.util.Arrays.fill(arr, 500000d);
        double sum = svc.dailyBalanceInterest(java.util.Arrays.stream(arr).boxed().toList(), rate);
        assertEquals(6250d, sum, 1e-6);
    }
}
