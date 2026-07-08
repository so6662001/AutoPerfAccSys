package com.steel.perf.web;

import com.steel.perf.calc.CalcEngine;
import com.steel.perf.calc.model.ComponentDef;
import com.steel.perf.calc.model.PayslipResult;
import com.steel.perf.calc.model.PlanDef;
import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.common.tenant.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 核算接口：按方案(核算项+汇总公式)与上下文变量执行核算，返回带下钻的绩效单。
 * 租户隔离；核算结果含幂等键与逐项追溯。
 */
@RestController
@RequestMapping("/api/calc")
public class CalcController {

    private final CalcEngine calcEngine;

    public CalcController(CalcEngine calcEngine) {
        this.calcEngine = calcEngine;
    }

    public record ComponentReq(String code, String expression, boolean includeInTotal) {
    }

    public record RunReq(String planCode, int version, List<ComponentReq> components,
                         String summaryExpr, Map<String, Double> context,
                         String period, String snapshotHash) {
    }

    @PostMapping("/run")
    public ApiResponse<PayslipResult> run(@RequestBody RunReq req) {
        String tenant = TenantContext.requireTenantId();
        List<ComponentDef> comps = req.components().stream()
                .map(c -> new ComponentDef(c.code(), c.expression(), c.includeInTotal()))
                .toList();
        PlanDef plan = new PlanDef(req.planCode(), req.version(), comps, req.summaryExpr());
        String snap = req.snapshotHash() == null ? "adhoc" : req.snapshotHash();
        PayslipResult result = calcEngine.calc(plan, req.context(), tenant, req.period(), snap);
        return ApiResponse.ok(result);
    }
}
