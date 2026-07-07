package com.steel.perf.engine.tier;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 阶梯/门槛/阶跃/封顶 引擎测试。 */
class TierEngineTest {

    private final TierEngine engine = new TierEngine();

    // 名亿吨位：整体档位 0-800@8 / 800-1000@10 / 1000-2000@12 / 2000+@12
    private List<TierRow> mingyiTons() {
        return List.of(
                new TierRow(0, 800d, 8d),
                new TierRow(800d, 1000d, 10d),
                new TierRow(1000d, 2000d, 12d),
                new TierRow(2000d, null, 12d)
        );
    }

    // 超扑越现货吨位阶梯（累进）：0-500@3 / 500-1000@5 / 1000+@8
    private List<TierRow> chaopuyueTons() {
        return List.of(
                new TierRow(0, 500d, 3d),
                new TierRow(500d, 1000d, 5d),
                new TierRow(1000d, null, 8d)
        );
    }

    @Test
    void wholeBand900() {
        assertEquals(9000d, engine.step(900d, mingyiTons(), TierEngine.Mode.WHOLE), 1e-9);
    }

    @Test
    void progressive800() {
        // 500*3 + 300*5 = 3000
        assertEquals(3000d, engine.step(800d, chaopuyueTons(), TierEngine.Mode.PROGRESSIVE), 1e-9);
    }

    @Test
    void stairDiff() {
        // (60-10)/10 = 5
        assertEquals(5d, engine.stair(60d, 10d, 10d), 1e-9);
        // 低于门槛
        assertEquals(0d, engine.stair(8d, 10d, 10d), 1e-9);
    }

    @Test
    void capPerTonProfit() {
        assertEquals(50d, engine.cap(120d, 50d), 1e-9);
        assertEquals(30d, engine.cap(30d, 50d), 1e-9);
    }
}
