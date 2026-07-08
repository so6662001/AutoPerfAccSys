package com.steel.perf.web;

import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.repo.jpa.AppealEntity;
import com.steel.perf.service.AppealService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 绩效申诉复核接口（租户隔离）。 */
@RestController
@RequestMapping("/api/appeal")
public class AppealController {

    private final AppealService svc;

    public AppealController(AppealService svc) {
        this.svc = svc;
    }

    public record SubmitReq(String period, Long empId, String item, String reason) {
    }

    @PostMapping
    public ApiResponse<AppealEntity> submit(@RequestBody SubmitReq req) {
        return ApiResponse.ok(svc.submit(req.period(), req.empId(), req.item(), req.reason()));
    }

    public record AdjudicateReq(String decision, String note) {
    }

    @PostMapping("/{appealNo}/adjudicate")
    public ApiResponse<AppealEntity> adjudicate(@PathVariable String appealNo, @RequestBody AdjudicateReq req) {
        return ApiResponse.ok(svc.adjudicate(appealNo, req.decision(), req.note()));
    }

    @GetMapping
    public ApiResponse<List<AppealEntity>> list() {
        return ApiResponse.ok(svc.list());
    }
}
