package com.steel.perf.web;

import com.steel.perf.common.api.ApiResponse;
import com.steel.perf.common.exception.BizException;
import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.repo.jpa.CommissionDetailEntity;
import com.steel.perf.repo.jpa.CommissionDetailJpaRepo;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 提成核算单明细（绩效明细表）：逐单提成，按周期 + 日期区间筛选（不跨月）。
 * 满足超扑越/岳洋通"绩效明细表按日期筛选、不跨月"要求。租户隔离。
 */
@RestController
@RequestMapping("/api/commission")
public class CommissionController {

    private final CommissionDetailJpaRepo repo;

    public CommissionController(CommissionDetailJpaRepo repo) {
        this.repo = repo;
    }

    public record DetailReq(String period, Long empId, String bizDate, String orderNo, String item, double amount) {
    }

    @PostMapping("/details")
    public ApiResponse<CommissionDetailEntity> add(@RequestBody DetailReq req) {
        YearMonth ym = parseMonth(req.period());
        CommissionDetailEntity e = new CommissionDetailEntity();
        e.setTenantId(TenantContext.requireTenantId());
        e.setPeriod(req.period());
        e.setEmpId(req.empId());
        e.setBizDate(java.time.LocalDate.parse(req.bizDate()));
        // 校验 bizDate 属于该周期（不跨月）
        if (!YearMonth.from(e.getBizDate()).equals(ym)) {
            throw new BizException("业务日期与周期不一致（不跨月）");
        }
        e.setOrderNo(req.orderNo());
        e.setItem(req.item());
        e.setAmount(req.amount());
        repo.save(e);
        return ApiResponse.ok(e);
    }

    /**
     * 明细查询：period=yyyy-MM，from/to 为当月日（1..31），区间限当月，天然不跨月。
     */
    @GetMapping("/details")
    public ApiResponse<List<CommissionDetailEntity>> details(
            @RequestParam String period,
            @RequestParam(defaultValue = "1") int from,
            @RequestParam(defaultValue = "31") int to) {
        YearMonth ym = parseMonth(period);
        if (from < 1 || to > ym.lengthOfMonth() || from > to) {
            throw new BizException("日期区间非法或跨月");
        }
        return ApiResponse.ok(repo.findByTenantIdAndPeriodAndBizDateBetween(
                TenantContext.requireTenantId(), period, ym.atDay(from), ym.atDay(to),
                Sort.by(Sort.Direction.ASC, "bizDate")));
    }

    private YearMonth parseMonth(String period) {
        try {
            return YearMonth.parse(period);
        } catch (DateTimeParseException e) {
            throw new BizException("周期格式应为 yyyy-MM");
        }
    }
}
