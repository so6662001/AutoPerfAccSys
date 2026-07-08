package com.steel.perf.integration;

import com.steel.perf.common.exception.BizException;
import com.steel.perf.common.tenant.TenantContext;
import com.steel.perf.integration.dsl.CompiledQuery;
import com.steel.perf.integration.dsl.MetricQuerySpec;
import com.steel.perf.integration.dsl.SafeSqlCompiler;
import com.steel.perf.integration.erp.ErpReader;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 取数服务：编译取数 DSL → 执行只读 SQL → 返回按核算对象聚合的结果。
 * 强制注入当前租户 tenantId 参数，校验参数完整；保证多租户隔离与注入防护。
 */
@Service
public class MetricQueryService {

    private final SafeSqlCompiler compiler;
    private final ErpReader reader;

    public MetricQueryService(SafeSqlCompiler compiler, ErpReader reader) {
        this.compiler = compiler;
        this.reader = reader;
    }

    /**
     * @param spec        取数规格
     * @param condParams  条件参数值（键须与 spec.conditions 的 paramName 一致）
     * @return 每行 subject + val
     */
    public List<Map<String, Object>> query(MetricQuerySpec spec, Map<String, Object> condParams) {
        String tenant = TenantContext.requireTenantId();
        CompiledQuery cq = compiler.compile(spec);
        Map<String, Object> bind = new HashMap<>(condParams == null ? Map.of() : condParams);
        bind.put("tenantId", tenant); // 强制租户隔离
        // 校验编译所需参数齐全
        for (String p : cq.paramOrder()) {
            if (!bind.containsKey(p)) {
                throw new BizException("缺少取数参数: " + p);
            }
        }
        return reader.query(cq.sql(), bind);
    }
}
