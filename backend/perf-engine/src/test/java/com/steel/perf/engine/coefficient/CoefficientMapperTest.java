package com.steel.perf.engine.coefficient;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 达成率映射测试：达成率->系数 与 达成率->固定金额。 */
class CoefficientMapperTest {

    private final CoefficientMapper mapper = new CoefficientMapper();

    // 系数档：<=60->0, <=80->0.6, <=100->0.8, <=120->1.0, 其他->1.2
    private List<Band> coefBands() {
        return List.of(
                new Band(60d, 0d),
                new Band(80d, 0.6d),
                new Band(100d, 0.8d),
                new Band(120d, 1.0d),
                new Band(null, 1.2d)
        );
    }

    // 超扑越基本工资金额档：<=80->2000, <=90->2500, <=100->3000, 其他->3500
    private List<Band> wageBands() {
        return List.of(
                new Band(80d, 2000d),
                new Band(90d, 2500d),
                new Band(100d, 3000d),
                new Band(null, 3500d)
        );
    }

    @Test
    void rateToCoefficient() {
        assertEquals(1.0d, mapper.map(108.2d, coefBands()), 1e-9); // 超扑越综合达成率 108.2 -> 1.0
        assertEquals(0d, mapper.map(59d, coefBands()), 1e-9);
        assertEquals(0.6d, mapper.map(70d, coefBands()), 1e-9);
        assertEquals(1.2d, mapper.map(130d, coefBands()), 1e-9);
    }

    @Test
    void rateToFixedWage() {
        assertEquals(3500d, mapper.map(102d, wageBands()), 1e-9); // 资深
        assertEquals(2500d, mapper.map(85d, wageBands()), 1e-9);  // 中级
        assertEquals(2000d, mapper.map(75d, wageBands()), 1e-9);  // 初级
    }
}
