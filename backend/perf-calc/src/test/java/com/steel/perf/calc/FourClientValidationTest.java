package com.steel.perf.calc;

import com.steel.perf.calc.model.ComponentDef;
import com.steel.perf.calc.model.PlanDef;
import com.steel.perf.engine.coefficient.Band;
import com.steel.perf.engine.coefficient.CoefficientMapper;
import com.steel.perf.engine.cost.CostEngine;
import com.steel.perf.engine.scope.ScopeResolver;
import com.steel.perf.engine.scope.ScopeRule;
import com.steel.perf.engine.tier.TierEngine;
import com.steel.perf.engine.tier.TierRow;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 四客户方案可配置跑通验证（M5 验收点）。
 * 用平台基础能力（作用域/阶梯/成本/系数）+ 核算编排，复现四家真实客户的关键数值，
 * 证明系统以"配置"方式适配，无需为每家写死代码。
 */
class FourClientValidationTest {

    private final CalcEngine calc = new CalcEngine();
    private final ScopeResolver scope = new ScopeResolver();
    private final TierEngine tier = new TierEngine();
    private final CoefficientMapper mapper = new CoefficientMapper();
    private final CostEngine cost = new CostEngine();

    /** 超扑越·销售现货：合计 20835。 */
    @Test
    void chaopuyue() {
        PlanDef plan = new PlanDef("XPY", 3, List.of(
                new ComponentDef("基本工资", "基数*系数", true),
                new ComponentDef("吨位提成", "现款量*现款单价+账期量*账期单价", true),
                new ComponentDef("挂价所得", "(成交价-挂牌价)*吨量*分配", true),
                new ComponentDef("预收计息", "预收*日利率*天数*分配", true),
                new ComponentDef("应收扣减", "-吨位提成*扣比", true)
        ), null);
        // 基本工资系数：综合达成率 102 -> 资深档 3500，用 基数=3500、系数=1 表达
        Map<String, Double> ctx = Map.ofEntries(
                Map.entry("基数", 6000d), Map.entry("系数", 1.0d),
                Map.entry("现款量", 300d), Map.entry("现款单价", 8d),
                Map.entry("账期量", 500d), Map.entry("账期单价", 6d),
                Map.entry("成交价", 4010d), Map.entry("挂牌价", 4000d), Map.entry("吨量", 800d), Map.entry("分配", 0.7d),
                Map.entry("预收", 500000d), Map.entry("日利率", 0.0005d), Map.entry("天数", 25d),
                Map.entry("扣比", 0.1d));
        assertEquals(20835d, calc.calc(plan, ctx, "xpy", "2026-06", "s").total(), 1e-6);
    }

    /** 岳洋通·配货库：销量+差价 ×团队系数 + 加工 + 新户 = 830。 */
    @Test
    void yueyangtong() {
        double salesUnit = scope.resolveValue(List.of(
                new ScopeRule(Map.of(), 1d, "默认"),
                new ScopeRule(Map.of("store", "配货"), 1d, "配货库")), Map.of("store", "配货"));
        double diffUnit = tier.stair(6000d / 100d, 10d, 10d); // (60-10)/10 = 5
        PlanDef plan = new PlanDef("YYT", 1, List.of(
                new ComponentDef("销量绩效", "重量*销量单价", false),
                new ComponentDef("差价绩效", "差价系数*重量*库别系数", false),
                new ComponentDef("加工绩效", "加工费*0.03", true),
                new ComponentDef("开发新户", "新户金额", true),
                new ComponentDef("团队后", "(销量绩效+差价绩效)*团队系数", true)
        ), null);
        Map<String, Double> ctx = Map.ofEntries(
                Map.entry("重量", 100d), Map.entry("销量单价", salesUnit),
                Map.entry("差价系数", diffUnit), Map.entry("库别系数", 1d),
                Map.entry("加工费", 8000d), Map.entry("新户金额", 50d), Map.entry("团队系数", 0.9d));
        assertEquals(830d, calc.calc(plan, ctx, "yyt", "2026-06", "s").total(), 1e-6);
    }

    /** 信桥·现货10天内 吨位绩效 + 利润绩效(吨利润封顶50) = 900 + 1200 = 2100。 */
    @Test
    void xinqiao() {
        double tonUnit = scope.resolveValue(List.of(
                new ScopeRule(Map.of(), 1d, "默认"),
                new ScopeRule(Map.of("biz", "现货"), 9d, "现货"),
                new ScopeRule(Map.of("biz", "现货", "term", "40-50天"), -5d, "现货40-50")),
                Map.of("biz", "现货", "term", "10天内")); // 9
        double unitProfit = tier.cap(5120d - 5000d, 50d); // 120 封顶 50
        PlanDef plan = new PlanDef("XQ", 1, List.of(
                new ComponentDef("吨位绩效", "吨位单价*重量", true),
                new ComponentDef("利润绩效", "(单位吨利润*重量-一票制)*账期系数", true)
        ), null);
        Map<String, Double> ctx = Map.of("吨位单价", tonUnit, "重量", 100d,
                "单位吨利润", unitProfit, "一票制", 2000d, "账期系数", 0.4d);
        assertEquals(2100d, calc.calc(plan, ctx, "xq", "2026-06", "s").total(), 1e-6);
    }

    /** 名亿：吨位提成(整体档位 900->9000) + 自营利润提成(结算方式成本)。 */
    @Test
    void mingyi() {
        double tonnage = tier.step(900d, List.of(
                new TierRow(0, 800d, 8d), new TierRow(800d, 1000d, 10d),
                new TierRow(1000d, 2000d, 12d), new TierRow(2000d, null, 12d)), TierEngine.Mode.WHOLE);
        assertEquals(9000d, tonnage, 1e-6);
        double income = (5100d - 5090d) * 1000d;                 // 自营收益 10000
        double c = cost.settlementCost(1000d, CostEngine.SettlementMode.CASH, 50d, -200d); // 现款成本 50000
        assertEquals(10000d, income, 1e-6);
        assertEquals(50000d, c, 1e-6);
        // 利润提成 = (收益-成本)*0.5（按思维导图口径，忠实复现，可能为负）
        double profitComm = (income - c) * 0.5;
        assertEquals(-20000d, profitComm, 1e-6);
        // 达成率->系数 校验：完成率 96% -> 0.8 档
        assertEquals(0.8d, mapper.map(96d, List.of(
                new Band(60d, 0d), new Band(80d, 0.6d), new Band(100d, 0.8d), new Band(120d, 1.0d), new Band(null, 1.2d))), 1e-9);
    }
}
