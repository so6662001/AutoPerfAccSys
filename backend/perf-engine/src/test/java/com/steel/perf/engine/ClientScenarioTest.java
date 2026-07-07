package com.steel.perf.engine;

import com.steel.perf.engine.coefficient.Band;
import com.steel.perf.engine.coefficient.CoefficientMapper;
import com.steel.perf.engine.formula.ExpressionEngine;
import com.steel.perf.engine.scope.ScopeResolver;
import com.steel.perf.engine.scope.ScopeRule;
import com.steel.perf.engine.tier.TierEngine;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 真实客户方案端到端组合校验（用引擎积木复现原型数值）。
 */
class ClientScenarioTest {

    private final ScopeResolver scope = new ScopeResolver();
    private final TierEngine tier = new TierEngine();
    private final CoefficientMapper mapper = new CoefficientMapper();
    private final ExpressionEngine formula = new ExpressionEngine();

    @Test
    void yueyangtongPeihuoStore() {
        // 岳洋通·配货库：销量绩效 + 差价绩效，×团队系数，+ 加工绩效 + 新户
        double salesUnit = scope.resolveValue(
                List.of(new ScopeRule(Map.of(), 1d, "默认"),
                        new ScopeRule(Map.of("store", "配货"), 1d, "配货库")),
                Map.of("store", "配货"));
        double weight = 100d;
        double salesPerf = weight * salesUnit;                 // 100
        double diffUnit = tier.stair(6000d / weight, 10d, 10d); // (60-10)/10 = 5
        double diffPerf = diffUnit * weight * 1d;               // 500
        double teamCoef = 0.9d;
        double afterTeam = (salesPerf + diffPerf) * teamCoef;   // 540
        double proc = formula.evaluate("fee * 0.03", Map.of("fee", 8000d)); // 240
        double newCust = 50d;
        double total = afterTeam + proc + newCust;
        assertEquals(830d, total, 1e-6);
    }

    @Test
    void xinqiaoTonnageMatrixNegative() {
        // 信桥·现货·40-50天：单价 -5，重量 100 -> -500
        double unit = scope.resolveValue(
                List.of(new ScopeRule(Map.of(), 1d, "默认"),
                        new ScopeRule(Map.of("biz", "现货"), 9d, "现货"),
                        new ScopeRule(Map.of("biz", "现货", "term", "40-50天"), -5d, "现货40-50")),
                Map.of("biz", "现货", "term", "40-50天"));
        assertEquals(-500d, unit * 100d, 1e-9);
    }

    @Test
    void chaopuyueBasicWage() {
        // 超扑越综合达成率 = 吨量105%*0.7 + 利润95%*0.3 = 102 -> 资深档 3500
        double comp = 105d * 0.7d + 95d * 0.3d;
        double wage = mapper.map(comp, List.of(
                new Band(80d, 2000d), new Band(90d, 2500d), new Band(100d, 3000d), new Band(null, 3500d)));
        assertEquals(3500d, wage, 1e-9);
    }

    @Test
    void mingyiTonnageWhole() {
        // 名亿完成 900 吨 -> 第三档整体 900*10 = 9000
        double amt = tier.step(900d, List.of(
                new com.steel.perf.engine.tier.TierRow(0, 800d, 8d),
                new com.steel.perf.engine.tier.TierRow(800d, 1000d, 10d),
                new com.steel.perf.engine.tier.TierRow(1000d, 2000d, 12d),
                new com.steel.perf.engine.tier.TierRow(2000d, null, 12d)), TierEngine.Mode.WHOLE);
        assertEquals(9000d, amt, 1e-9);
    }
}
