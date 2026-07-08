package com.steel.perf.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steel.perf.calc.CalcEngine;
import com.steel.perf.calc.model.ComponentDef;
import com.steel.perf.calc.model.PayslipResult;
import com.steel.perf.calc.model.PlanDef;
import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.repo.jpa.PayslipEntity;
import com.steel.perf.repo.jpa.PayslipJpaRepo;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 核算接口：按方案(核算项+汇总公式)与上下文变量执行核算，返回带下钻的绩效单。
 * 租户隔离；核算结果含幂等键与逐项追溯；结果持久化并按幂等键去重（重跑不重复入库）。
 */
@RestController
@RequestMapping("/api/calc")
public class CalcController {

    private final CalcEngine calcEngine;
    private final PayslipJpaRepo payslipRepo;
    private final ObjectMapper om;

    public CalcController(CalcEngine calcEngine, PayslipJpaRepo payslipRepo, ObjectMapper om) {
        this.calcEngine = calcEngine;
        this.payslipRepo = payslipRepo;
        this.om = om;
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
        persistIdempotent(tenant, req.period(), result);
        return ApiResponse.ok(result);
    }

    /** 幂等入库：同 idempotencyKey 已存在则不重复保存。 */
    private void persistIdempotent(String tenant, String period, PayslipResult r) {
        if (payslipRepo.findByTenantIdAndIdempotencyKey(tenant, r.idempotencyKey()).isPresent()) {
            return;
        }
        PayslipEntity e = new PayslipEntity();
        e.setTenantId(tenant);
        e.setIdempotencyKey(r.idempotencyKey());
        e.setPeriod(period);
        e.setPlanCode(r.planCode());
        e.setPlanVersion(r.planVersion());
        e.setTotal(r.total());
        try {
            e.setDetailJson(om.writeValueAsString(r.traces()));
        } catch (Exception ignore) {
            e.setDetailJson(null);
        }
        payslipRepo.save(e);
    }

    @GetMapping("/payslips")
    public ApiResponse<List<PayslipEntity>> payslips(@RequestParam String period) {
        return ApiResponse.ok(payslipRepo.findByTenantIdAndPeriod(TenantContext.requireTenantId(), period));
    }
}
