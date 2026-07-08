package com.steel.perf.integration.erp;

import java.util.List;
import java.util.Map;

/**
 * ERP 只读取数接口（按租户路由到各自 SQL Server 2008+ 只读连接）。
 * 实现须：只读连接、参数绑定、查询超时、返回行数上限；严禁非 SELECT。
 * M1 提供接口与内存实现（测试用），生产实现走 JdbcTemplate/MyBatis + 只读数据源。
 */
public interface ErpReader {

    /**
     * 执行参数化只读查询。
     *
     * @param sql    仅含命名参数占位的 SELECT（由 SafeSqlCompiler 生成）
     * @param params 命名参数值
     * @return 结果行（列名 -> 值）
     */
    List<Map<String, Object>> query(String sql, Map<String, Object> params);
}
