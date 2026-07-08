package com.steel.perf.calc;

import com.steel.perf.calc.model.ComponentDef;
import com.steel.perf.calc.model.ComponentTrace;
import com.steel.perf.calc.model.PayslipResult;
import com.steel.perf.calc.model.PlanDef;
import com.steel.perf.common.exception.BizException;
import com.steel.perf.engine.formula.ExpressionEngine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 核算编排引擎（M2）。
 * 按依赖拓扑排序执行核算项（核算项可引用指标/参数/其他核算项），
 * 逐项用公式引擎计算并记录追溯明细，最后汇总，产出可复算、可下钻的绩效单。
 */
public class CalcEngine {

    private final ExpressionEngine formula;

    public CalcEngine(ExpressionEngine formula) {
        this.formula = formula;
    }

    public CalcEngine() {
        this(new ExpressionEngine());
    }

    /**
     * 执行核算。
     *
     * @param plan         方案（核算项 + 汇总公式）
     * @param context      指标/参数变量（如 基数、系数、成交价、预收、日利率…）
     * @param tenantId     租户
     * @param period       核算周期（如 2026-06）
     * @param snapshotHash 数据快照哈希（用于幂等/复算）
     */
    public PayslipResult calc(PlanDef plan, Map<String, Double> context,
                              String tenantId, String period, String snapshotHash) {
        if (plan == null || plan.components() == null || plan.components().isEmpty()) {
            throw new BizException("方案或核算项为空");
        }
        List<ComponentDef> ordered = topoSort(plan.components());

        Map<String, Double> vars = new HashMap<>(context == null ? Map.of() : context);
        Map<String, Double> componentAmounts = new LinkedHashMap<>();
        List<ComponentTrace> traces = new ArrayList<>();

        for (ComponentDef c : ordered) {
            double amt = formula.evaluate(c.expression(), vars);
            componentAmounts.put(c.code(), amt);
            vars.put(c.code(), amt); // 供后续核算项引用
            traces.add(new ComponentTrace(c.code(), c.expression(), usedVars(c.expression(), vars), amt));
        }

        double total = computeTotal(plan, componentAmounts, vars);
        String key = IdempotencyKey.of(tenantId, period, plan.version(), snapshotHash);
        return new PayslipResult(plan.code(), plan.version(), componentAmounts, traces, total, key);
    }

    private double computeTotal(PlanDef plan, Map<String, Double> amounts, Map<String, Double> vars) {
        if (plan.summaryExpr() != null && !plan.summaryExpr().isBlank()) {
            return formula.evaluate(plan.summaryExpr(), vars);
        }
        double t = 0d;
        for (ComponentDef c : plan.components()) {
            if (c.includeInTotal()) {
                t += amounts.getOrDefault(c.code(), 0d);
            }
        }
        return t;
    }

    /** 按依赖（核算项引用其他核算项编码）拓扑排序；发现环报错。 */
    private List<ComponentDef> topoSort(List<ComponentDef> comps) {
        Map<String, ComponentDef> byCode = new LinkedHashMap<>();
        for (ComponentDef c : comps) {
            if (byCode.containsKey(c.code())) {
                throw new BizException("核算项编码重复: " + c.code());
            }
            byCode.put(c.code(), c);
        }
        Map<String, List<String>> deps = new LinkedHashMap<>();
        Map<String, Integer> indeg = new LinkedHashMap<>();
        for (ComponentDef c : comps) {
            List<String> d = new ArrayList<>();
            for (String other : byCode.keySet()) {
                if (!other.equals(c.code()) && referencesToken(c.expression(), other)) {
                    d.add(other);
                }
            }
            deps.put(c.code(), d);
            indeg.put(c.code(), d.size()); // 入度 = 依赖的其他核算项个数
        }
        Deque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> e : indeg.entrySet()) {
            if (e.getValue() == 0) {
                queue.add(e.getKey());
            }
        }
        List<ComponentDef> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String code = queue.poll();
            result.add(byCode.get(code));
            for (Map.Entry<String, List<String>> e : deps.entrySet()) {
                if (e.getValue().contains(code)) {
                    int v = indeg.merge(e.getKey(), -1, Integer::sum);
                    if (v == 0) {
                        queue.add(e.getKey());
                    }
                }
            }
        }
        if (result.size() != comps.size()) {
            throw new BizException("核算项存在循环依赖");
        }
        return result;
    }

    /** 记录公式中引用到的变量（含依赖核算项）的取值，供下钻。 */
    private Map<String, Double> usedVars(String expr, Map<String, Double> vars) {
        Map<String, Double> used = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : vars.entrySet()) {
            if (referencesToken(expr, e.getKey())) {
                used.put(e.getKey(), e.getValue());
            }
        }
        return used;
    }

    /** 判断表达式是否以"完整标识符"引用了 token（前后非标识符字符），避免子串误判。 */
    private boolean referencesToken(String expr, String token) {
        if (expr == null || token == null || token.isEmpty()) {
            return false;
        }
        int idx = 0;
        while ((idx = expr.indexOf(token, idx)) >= 0) {
            char before = idx == 0 ? ' ' : expr.charAt(idx - 1);
            int after = idx + token.length();
            char next = after >= expr.length() ? ' ' : expr.charAt(after);
            if (!isIdentChar(before) && !isIdentChar(next)) {
                return true;
            }
            idx = after;
        }
        return false;
    }

    private boolean isIdentChar(char c) {
        // 汉字属于字母（isLetterOrDigit 为 true），故中文核算项编码也按完整标识符边界判断
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }
}
