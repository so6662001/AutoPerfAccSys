package com.steel.perf.engine.scope;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 多维参数作用域解析器测试（信桥/岳洋通等分档差异）。 */
class ScopeResolverTest {

    private final ScopeResolver resolver = new ScopeResolver();

    private List<ScopeRule> xinqiaoRules() {
        return List.of(
                new ScopeRule(Map.of(), 1d, "默认"),
                new ScopeRule(Map.of("biz", "现货"), 9d, "信桥·现货"),
                new ScopeRule(Map.of("biz", "现货", "term", "40-50天"), -5d, "信桥·现货·40-50天"),
                new ScopeRule(Map.of("biz", "外调"), 6d, "信桥·外调"),
                new ScopeRule(Map.of("org", "华东", "cat", "板材", "store", "配货"), 1.5d, "岳洋通·华东·板材·配货")
        );
    }

    @Test
    void mostSpecificWins() {
        assertEquals(-5d, resolver.resolveValue(xinqiaoRules(),
                Map.of("biz", "现货", "term", "40-50天")));
    }

    @Test
    void fallbackToLessSpecific() {
        assertEquals(9d, resolver.resolveValue(xinqiaoRules(),
                Map.of("biz", "现货", "term", "10天内")));
    }

    @Test
    void fallbackToDefault() {
        assertEquals(1d, resolver.resolveValue(xinqiaoRules(),
                Map.of("biz", "内部")));
    }

    @Test
    void threeDimOverride() {
        assertEquals(1.5d, resolver.resolveValue(xinqiaoRules(),
                Map.of("org", "华东", "cat", "板材", "store", "配货")));
    }
}
