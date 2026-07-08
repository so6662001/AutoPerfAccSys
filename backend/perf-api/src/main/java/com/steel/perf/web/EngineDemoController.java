package com.steel.perf.web;

import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.engine.formula.ExpressionEngine;
import com.steel.perf.engine.scope.ScopeResolver;
import com.steel.perf.engine.scope.ScopeRule;
import com.steel.perf.engine.tier.TierEngine;
import com.steel.perf.engine.tier.TierRow;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 核算引擎演示接口（M0）：验证多维作用域 / 阶梯 / 公式引擎可用。
 * 正式核算接口将在 perf-calc / perf-rule 落地。
 */
@RestController
@RequestMapping("/api/engine")
public class EngineDemoController {

    private final ScopeResolver scopeResolver;
    private final TierEngine tierEngine;
    private final ExpressionEngine expressionEngine;

    public EngineDemoController(ScopeResolver scopeResolver, TierEngine tierEngine, ExpressionEngine expressionEngine) {
        this.scopeResolver = scopeResolver;
        this.tierEngine = tierEngine;
        this.expressionEngine = expressionEngine;
    }

    @GetMapping("/whoami")
    public ApiResponse<Map<String, Object>> whoami() {
        String tenant = TenantContext.requireTenantId();
        return ApiResponse.ok(Map.of("tenantId", tenant, "userId",
                String.valueOf(TenantContext.currentUserId())));
    }

    public record FormulaReq(String expression, Map<String, Double> vars) {
    }

    @PostMapping("/formula")
    public ApiResponse<Double> formula(@RequestBody FormulaReq req) {
        return ApiResponse.ok(expressionEngine.evaluate(req.expression(), req.vars()));
    }

    public record TierRowDto(double lower, Double upper, double factor) {
    }

    public record TierReq(double base, List<TierRowDto> rows, String mode) {
    }

    @PostMapping("/tier/step")
    public ApiResponse<Double> tierStep(@RequestBody TierReq req) {
        List<TierRow> rows = req.rows().stream()
                .map(r -> new TierRow(r.lower(), r.upper(), r.factor())).toList();
        TierEngine.Mode mode = "PROGRESSIVE".equalsIgnoreCase(req.mode())
                ? TierEngine.Mode.PROGRESSIVE : TierEngine.Mode.WHOLE;
        return ApiResponse.ok(tierEngine.step(req.base(), rows, mode));
    }

    public record ScopeRuleDto(Map<String, String> conditions, double value, String source) {
    }

    public record ScopeReq(List<ScopeRuleDto> rules, Map<String, String> context) {
    }

    @PostMapping("/scope/resolve")
    public ApiResponse<Map<String, Object>> scopeResolve(@RequestBody ScopeReq req) {
        List<ScopeRule> rules = req.rules().stream()
                .map(r -> new ScopeRule(r.conditions(), r.value(), r.source())).toList();
        ScopeRule hit = scopeResolver.resolve(rules, req.context());
        return ApiResponse.ok(Map.of("value", hit.getValue(), "source", hit.getSource(),
                "specificity", hit.specificity()));
    }
}
