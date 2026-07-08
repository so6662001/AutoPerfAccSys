package com.steel.perf.web;

import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.integration.MetricQueryService;
import com.steel.perf.integration.dsl.MetricQuerySpec;
import com.steel.perf.metric.ContractInterestService;
import com.steel.perf.metric.MetricService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 指标 / 取数 / 计息 REST 接口。
 * 取数经 SafeSqlCompiler 安全编译 + 每租户 ERP 只读路由执行；计息为纯计算。
 */
@RestController
@RequestMapping("/api/metric")
public class MetricController {

    private final MetricQueryService queryService;
    private final MetricService metricService;
    private final ContractInterestService contractInterestService;

    public MetricController(MetricQueryService queryService, MetricService metricService,
                            ContractInterestService contractInterestService) {
        this.queryService = queryService;
        this.metricService = metricService;
        this.contractInterestService = contractInterestService;
    }

    public record QueryReq(MetricQuerySpec spec, Map<String, Object> params) {
    }

    /** 取数：配置化 DSL → 安全 SQL → 执行（租户路由）。 */
    @PostMapping("/query")
    public ApiResponse<List<Map<String, Object>>> query(@RequestBody QueryReq req) {
        return ApiResponse.ok(queryService.query(req.spec(), req.params()));
    }

    public record DailyInterestReq(List<Double> dailyBalances, double dayRate) {
    }

    /** 逐日计息（预收/应收/预付/应付）。 */
    @PostMapping("/interest/daily")
    public ApiResponse<Double> dailyInterest(@RequestBody DailyInterestReq req) {
        return ApiResponse.ok(metricService.dailyBalanceInterest(req.dailyBalances(), req.dayRate()));
    }

    public record SegDto(double amount, int days) {
    }

    public record ContractInterestReq(List<SegDto> segments, double dayRate) {
    }

    /** 期货合同滚动计息（定金/货物分笔）。 */
    @PostMapping("/interest/contract")
    public ApiResponse<Double> contractInterest(@RequestBody ContractInterestReq req) {
        List<ContractInterestService.Segment> segs = req.segments().stream()
                .map(s -> new ContractInterestService.Segment(s.amount(), s.days())).toList();
        return ApiResponse.ok(contractInterestService.interest(segs, req.dayRate()));
    }
}
