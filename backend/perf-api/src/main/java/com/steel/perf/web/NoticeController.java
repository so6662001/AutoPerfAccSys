package com.steel.perf.web;

import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.repo.jpa.NoticeEntity;
import com.steel.perf.service.NoticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 员工端消息中心（租户隔离）。 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService svc;

    public NoticeController(NoticeService svc) {
        this.svc = svc;
    }

    @GetMapping
    public ApiResponse<List<NoticeEntity>> list() {
        return ApiResponse.ok(svc.list());
    }
}
