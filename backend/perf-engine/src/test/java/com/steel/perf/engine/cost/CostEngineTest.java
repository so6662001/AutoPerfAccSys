package com.steel.perf.engine.cost;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 成本口径引擎测试（名亿结算方式驱动成本）。 */
class CostEngineTest {

    private final CostEngine engine = new CostEngine();

    @Test
    void settlementModes() {
        assertEquals(50000d, engine.settlementCost(1000d, CostEngine.SettlementMode.CASH, 50d, -200d), 1e-9);
        assertEquals(0d, engine.settlementCost(1000d, CostEngine.SettlementMode.CREDIT, 50d, -200d), 1e-9);
        assertEquals(-200d, engine.settlementCost(1000d, CostEngine.SettlementMode.ADVANCE, 50d, -200d), 1e-9);
    }

    @Test
    void adjustedGross() {
        assertEquals(9300d, engine.adjustedGross(10000d, 500d, 200d), 1e-9);
    }
}
