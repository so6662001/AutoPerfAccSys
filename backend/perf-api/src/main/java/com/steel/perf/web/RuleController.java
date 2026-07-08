package com.steel.perf.web;

import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.common.exception.BizException;
import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.repo.ChangeRequestStore;
import com.steel.perf.rule.ChangeRequestService;
import com.steel.perf.rule.ParamDiff;
import com.steel.perf.rule.PublishValidator;
import com.steel.perf.rule.model.ChangeRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 规则治理接口：变更单提交/审批(三级串签)/退回/委托/加签/到期生效/灰度推广/版本diff。
 * 全部租户隔离。
 */
@RestController
@RequestMapping("/api/rule")
public class RuleController {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private final ChangeRequestService svc;
    private final PublishValidator validator;
    private final ParamDiff paramDiff;
    private final ChangeRequestStore repo;

    public RuleController(ChangeRequestService svc, PublishValidator validator,
                          ParamDiff paramDiff, ChangeRequestStore repo) {
        this.svc = svc;
        this.validator = validator;
        this.paramDiff = paramDiff;
        this.repo = repo;
    }

    public record SubmitReq(String planName, String effMode, String effLabel, String scopeLabel,
                            List<String> details, Map<String, String> params,
                            Map<String, Integer> scorecardWeightSums) {
    }

    @PostMapping("/change-requests")
    public ApiResponse<ChangeRequest> submit(@RequestBody SubmitReq req) {
        String tenant = TenantContext.requireTenantId();
        validator.validateWeights(req.scorecardWeightSums()); // 发布硬校验
        int ver = repo.nextVersion(tenant);
        String id = "CR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%02d", ver);
        ChangeRequest cr = new ChangeRequest();
        cr.setId(id);
        cr.setVer(ver);
        cr.setSubmitter(TenantContext.currentUserId());
        cr.setEffMode("SCHEDULED".equalsIgnoreCase(req.effMode())
                ? ChangeRequest.EffMode.SCHEDULED : ChangeRequest.EffMode.NOW);
        if (req.effLabel() != null) cr.setEffLabel(req.effLabel());
        if (req.scopeLabel() != null) cr.setScopeLabel(req.scopeLabel());
        cr.setChain(svc.defaultChain());
        cr.setDetails(req.details());
        cr.setParams(req.params());
        repo.save(tenant, cr);
        return ApiResponse.ok(cr);
    }

    @GetMapping("/change-requests")
    public ApiResponse<List<ChangeRequest>> list() {
        return ApiResponse.ok(repo.list(TenantContext.requireTenantId()));
    }

    private ChangeRequest require(String id) {
        ChangeRequest cr = repo.findById(TenantContext.requireTenantId(), id);
        if (cr == null) {
            throw new BizException(404, "变更单不存在: " + id);
        }
        return cr;
    }

    public record ApproveReq(String approver) {
    }

    @PostMapping("/change-requests/{id}/approve")
    public ApiResponse<ChangeRequest> approve(@PathVariable String id, @RequestBody ApproveReq req) {
        ChangeRequest cr = require(id);
        svc.approve(cr, req.approver(), LocalDateTime.now().format(TF));
        repo.save(TenantContext.requireTenantId(), cr);
        return ApiResponse.ok(cr);
    }

    @PostMapping("/change-requests/{id}/reject")
    public ApiResponse<ChangeRequest> reject(@PathVariable String id) {
        ChangeRequest cr = require(id);
        svc.reject(cr, LocalDateTime.now().format(TF));
        repo.save(TenantContext.requireTenantId(), cr);
        return ApiResponse.ok(cr);
    }

    public record DelegateReq(String newApprover) {
    }

    @PostMapping("/change-requests/{id}/delegate")
    public ApiResponse<ChangeRequest> delegate(@PathVariable String id, @RequestBody DelegateReq req) {
        ChangeRequest cr = require(id);
        svc.delegate(cr, req.newApprover());
        repo.save(TenantContext.requireTenantId(), cr);
        return ApiResponse.ok(cr);
    }

    public record AddSignReq(String role, String name) {
    }

    @PostMapping("/change-requests/{id}/add-sign")
    public ApiResponse<ChangeRequest> addSign(@PathVariable String id, @RequestBody AddSignReq req) {
        ChangeRequest cr = require(id);
        svc.addSign(cr, req.role(), req.name());
        repo.save(TenantContext.requireTenantId(), cr);
        return ApiResponse.ok(cr);
    }

    @PostMapping("/change-requests/{id}/go-effective")
    public ApiResponse<ChangeRequest> goEffective(@PathVariable String id) {
        ChangeRequest cr = require(id);
        svc.goEffective(cr);
        repo.save(TenantContext.requireTenantId(), cr);
        return ApiResponse.ok(cr);
    }

    @PostMapping("/change-requests/{id}/promote")
    public ApiResponse<ChangeRequest> promote(@PathVariable String id) {
        String tenant = TenantContext.requireTenantId();
        ChangeRequest gray = require(id);
        int ver = repo.nextVersion(tenant);
        String nid = "CR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%02d", ver);
        ChangeRequest full = svc.promote(gray, nid, ver);
        repo.save(tenant, full);
        return ApiResponse.ok(full);
    }

    @GetMapping("/diff")
    public ApiResponse<List<ParamDiff.DiffItem>> diff(@RequestParam String a, @RequestParam String b) {
        ChangeRequest ra = require(a);
        ChangeRequest rb = require(b);
        return ApiResponse.ok(paramDiff.diff(ra.getParams(), rb.getParams()));
    }
}
