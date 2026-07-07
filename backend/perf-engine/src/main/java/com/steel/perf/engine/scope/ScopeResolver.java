package com.steel.perf.engine.scope;

import com.steel.perf.common.exception.BizException;

import java.util.List;
import java.util.Map;

/**
 * 多维参数作用域解析器（平台基础能力 P0）。
 * 运行时按"默认 -> 维度组合"逐级覆盖，命中"所有条件都满足"的规则中具体度最高者。
 * 覆盖 信桥(业务×账期×材质×品类×供应商)、岳洋通(库别×分支×类别)、超扑越(时段×组别×客户类型)、名亿(业务类型)。
 */
public class ScopeResolver {

    /**
     * 解析生效规则。
     *
     * @param rules   规则集（含默认规则，默认规则条件为空恒匹配）
     * @param context 业务上下文（维度=值）
     * @return 命中的最具体规则
     */
    public ScopeRule resolve(List<ScopeRule> rules, Map<String, String> context) {
        if (rules == null || rules.isEmpty()) {
            throw new BizException("作用域规则为空");
        }
        ScopeRule best = null;
        int bestSpec = -1;
        for (ScopeRule r : rules) {
            if (r.matches(context) && r.specificity() >= bestSpec) {
                // 具体度相同则后者覆盖（配置顺序即优先级微调）
                bestSpec = r.specificity();
                best = r;
            }
        }
        if (best == null) {
            throw new BizException("未命中任何作用域规则（缺少默认规则？）");
        }
        return best;
    }

    /** 便捷方法：直接返回生效值。 */
    public double resolveValue(List<ScopeRule> rules, Map<String, String> context) {
        return resolve(rules, context).getValue();
    }
}
