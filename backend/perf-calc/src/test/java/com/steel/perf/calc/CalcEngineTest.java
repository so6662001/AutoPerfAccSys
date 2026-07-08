package com.steel.perf.calc;

import com.steel.perf.calc.model.ComponentDef;
import com.steel.perf.calc.model.PayslipResult;
import com.steel.perf.calc.model.PlanDef;
import com.steel.perf.common.exception.BizException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalcEngineTest {

    private final CalcEngine engine = new CalcEngine();

    /** 复现超扑越销售现货绩效单：合计 20835，含依赖(应收扣减依赖吨位提成)与下钻。 */
    @Test
    void chaopuyuePayslip() {
        PlanDef plan = new PlanDef("XPY-XIANHUO", 3, List.of(
                new ComponentDef("基本工资", "基数 * 基础系数", true),
                new ComponentDef("吨位提成", "现款量*现款单价 + 账期量*账期单价", true),
                new ComponentDef("挂价所得", "(成交价-挂牌价)*吨量*分配系数", true),
                new ComponentDef("预收计息", "预收*日利率*占用天数*分配系数", true),
                // 依赖"吨位提成"
                new ComponentDef("应收扣减", "-吨位提成 * 扣款比例", true)
        ), null);

        Map<String, Double> ctx = Map.ofEntries(
                Map.entry("基数", 6000d), Map.entry("基础系数", 1.0d),
                Map.entry("现款量", 300d), Map.entry("现款单价", 8d),
                Map.entry("账期量", 500d), Map.entry("账期单价", 6d),
                Map.entry("成交价", 4010d), Map.entry("挂牌价", 4000d), Map.entry("吨量", 800d),
                Map.entry("分配系数", 0.7d),
                Map.entry("预收", 500000d), Map.entry("日利率", 0.0005d), Map.entry("占用天数", 25d),
                Map.entry("扣款比例", 0.1d)
        );

        PayslipResult r = engine.calc(plan, ctx, "chaopuyue", "2026-06", "snap-abc");
        assertEquals(6000d, r.componentAmounts().get("基本工资"), 1e-6);
        assertEquals(5400d, r.componentAmounts().get("吨位提成"), 1e-6);
        assertEquals(5600d, r.componentAmounts().get("挂价所得"), 1e-6);
        assertEquals(4375d, r.componentAmounts().get("预收计息"), 1e-6);
        assertEquals(-540d, r.componentAmounts().get("应收扣减"), 1e-6);
        assertEquals(20835d, r.total(), 1e-6);

        // 下钻：应收扣减引用了"吨位提成"结果
        var trace = r.traces().stream().filter(t -> t.code().equals("应收扣减")).findFirst().orElseThrow();
        assertEquals(5400d, trace.usedVars().get("吨位提成"), 1e-6);
        assertTrue(trace.usedVars().containsKey("扣款比例"));
    }

    @Test
    void idempotencyKeyStable() {
        PlanDef plan = new PlanDef("P", 1, List.of(new ComponentDef("a", "1", true)), null);
        String k1 = engine.calc(plan, Map.of(), "t1", "2026-06", "h1").idempotencyKey();
        String k2 = engine.calc(plan, Map.of(), "t1", "2026-06", "h1").idempotencyKey();
        String k3 = engine.calc(plan, Map.of(), "t1", "2026-06", "h2").idempotencyKey();
        assertEquals(k1, k2);
        assertTrue(!k1.equals(k3));
    }

    @Test
    void detectCyclicDependency() {
        PlanDef plan = new PlanDef("P", 1, List.of(
                new ComponentDef("甲", "乙 + 1", true),
                new ComponentDef("乙", "甲 + 1", true)
        ), null);
        assertThrows(BizException.class, () -> engine.calc(plan, Map.of(), "t", "p", "h"));
    }

    @Test
    void summaryExpressionOverride() {
        PlanDef plan = new PlanDef("P", 1, List.of(
                new ComponentDef("a", "10", true),
                new ComponentDef("b", "20", false)
        ), "a + b * 2");
        PayslipResult r = engine.calc(plan, Map.of(), "t", "p", "h");
        assertEquals(50d, r.total(), 1e-9); // 10 + 20*2
    }
}
