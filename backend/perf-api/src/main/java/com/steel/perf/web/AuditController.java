package com.steel.perf.web;

import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.repo.jpa.AuditLogEntity;
import com.steel.perf.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 合规审计查询（租户隔离）。 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService audit;

    public AuditController(AuditService audit) {
        this.audit = audit;
    }

    @GetMapping
    public ApiResponse<List<AuditLogEntity>> list() {
        return ApiResponse.ok(audit.list());
    }
}
