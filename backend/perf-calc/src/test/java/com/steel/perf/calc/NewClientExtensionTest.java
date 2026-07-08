package com.steel.perf.calc;

import com.steel.perf.calc.model.ComponentDef;
import com.steel.perf.calc.model.PlanDef;
import com.steel.perf.engine.coefficient.Band;
import com.steel.perf.engine.coefficient.CoefficientMapper;
import com.steel.perf.engine.scope.ScopeResolver;
import com.steel.perf.engine.scope.ScopeRule;
import com.steel.perf.engine.tier.TierEngine;
import com.steel.perf.engine.tier.TierRow;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 新增客户「纯配置」接入验证：证明后续新客户无需改代码，只用平台基础能力
 * （作用域参数 / 达成率映射 / 阶梯 / 计息表达式）+ 方案编排即可跑通。
 *
 * 示例客户「鑫源钢贸·区域直销」考核口径：
 *   基本工资 = 基数 × 达成系数(达成率→系数映射)
 *   吨位提成 = 销量 × 区域单价(按区域作用域取单价)
 *   阶梯奖金 = 阶梯step(销量, 整体档) 的整段计价
 *   利润分成 = 毛利 × 分成比
 *   计息扣减 = -(逾期应收 × 日利率 × 天数)
 *
 * 全流程仅通过配置（ScopeRule / Band / TierRow / PlanDef 表达式）表达，
 * 与四家既有客户共用同一套引擎与核算编排。
 */
class NewClientExtensionTest {

    private final CalcEngine calc = new CalcEngine();
    private final ScopeResolver scope = new ScopeResolver();
    private final CoefficientMapper mapper = new CoefficientMapper();
    private final TierEngine tier = new TierEngine();

    @Test
    void xinyuan_configOnly() {
        // 1) 达成率 105% -> 系数 1.0（达成率映射能力，配置 Band 即可）
        double achieveCoef = mapper.map(105d, List.of(
                new Band(60d, 0d), new Band(80d, 0.6d),
                new Band(100d, 0.8d), new Band(120d, 1.0d), new Band(null, 1.2d)));
        assertEquals(1.0d, achieveCoef, 1e-9);

        // 2) 区域=华东 -> 吨位单价 7（多维作用域参数能力，配置 ScopeRule 即可）
        double regionUnit = scope.resolveValue(List.of(
                new ScopeRule(Map.of(), 5d, "默认"),
                new ScopeRule(Map.of("region", "华东"), 7d, "华东"),
                new ScopeRule(Map.of("region", "华南"), 6d, "华南")),
                Map.of("region", "华东"));
        assertEquals(7d, regionUnit, 1e-9);

        // 3) 销量 900 吨落 [800,1200) 档，单价 10，整段计价 = 9000（阶梯能力，配置 TierRow 即可）
        double stairBonus = tier.step(900d, List.of(
                new TierRow(0, 800d, 8d), new TierRow(800d, 1200d, 10d),
                new TierRow(1200d, null, 12d)), TierEngine.Mode.WHOLE);
        assertEquals(9000d, stairBonus, 1e-6);

        // 4) 方案编排：与既有客户共用同一 CalcEngine，仅表达式不同
        PlanDef plan = new PlanDef("XINYUAN", 1, List.of(
                new ComponentDef("基本工资", "基数*达成系数", true),
                new ComponentDef("吨位提成", "销量*区域单价", true),
                new ComponentDef("阶梯奖金", "阶梯值", true),
                new ComponentDef("利润分成", "毛利*分成比", true),
                new ComponentDef("计息扣减", "-逾期应收*日利率*天数", true)
        ), null);

        Map<String, Double> ctx = Map.ofEntries(
                Map.entry("基数", 5000d), Map.entry("达成系数", achieveCoef),
                Map.entry("销量", 900d), Map.entry("区域单价", regionUnit),
                Map.entry("阶梯值", stairBonus),
                Map.entry("毛利", 40000d), Map.entry("分成比", 0.05d),
                Map.entry("逾期应收", 200000d), Map.entry("日利率", 0.0004d), Map.entry("天数", 25d));

        // 5000 + 6300 + 9000 + 2000 - 2000 = 20300
        assertEquals(20300d, calc.calc(plan, ctx, "xinyuan", "2026-06", "s").total(), 1e-6);
    }
}
