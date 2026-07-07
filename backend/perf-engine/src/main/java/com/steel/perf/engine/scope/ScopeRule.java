package com.steel.perf.engine.scope;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多维参数作用域规则：一组维度条件 -> 参数值。
 * 维度示例：业务类型/账期/库别/材质/品类/供应商/分支机构/岗位/时间段/客户类型。
 */
public class ScopeRule {

    private final Map<String, String> conditions;
    private final double value;
    private final String source;

    public ScopeRule(Map<String, String> conditions, double value, String source) {
        this.conditions = conditions == null ? Collections.emptyMap() : new LinkedHashMap<>(conditions);
        this.value = value;
        this.source = source;
    }

    /** 条件数量 = 具体度，越大越优先。 */
    public int specificity() {
        return conditions.size();
    }

    /** 该规则是否匹配给定上下文（所有条件都等于上下文对应值）。 */
    public boolean matches(Map<String, String> context) {
        for (Map.Entry<String, String> e : conditions.entrySet()) {
            String ctxVal = context == null ? null : context.get(e.getKey());
            if (!e.getValue().equals(ctxVal)) {
                return false;
            }
        }
        return true;
    }

    public Map<String, String> getConditions() {
        return conditions;
    }

    public double getValue() {
        return value;
    }

    public String getSource() {
        return source;
    }
}
