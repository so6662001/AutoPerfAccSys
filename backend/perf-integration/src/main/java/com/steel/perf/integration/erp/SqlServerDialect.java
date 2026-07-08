package com.steel.perf.integration.erp;

import com.steel.perf.common.exception.BizException;

/**
 * SQL Server 方言助手。兼容 SQL Server 2008（无 OFFSET/FETCH，使用 ROW_NUMBER 分页）。
 */
public final class SqlServerDialect {

    private SqlServerDialect() {
    }

    /**
     * 为 SELECT 语句包装 2008 兼容分页（ROW_NUMBER）。
     *
     * @param innerSelectColumns 内层选择列（不含 ORDER BY）
     * @param fromWhere          FROM ... WHERE ... 片段
     * @param orderBy            排序列（必填，ROW_NUMBER 需要）
     * @param offset            偏移（>=0）
     * @param size              每页大小（>0）
     */
    public static String paginate(String innerSelectColumns, String fromWhere, String orderBy,
                                  int offset, int size) {
        if (orderBy == null || orderBy.isBlank()) {
            throw new BizException("SQL Server 2008 分页必须指定 ORDER BY 列");
        }
        if (offset < 0 || size <= 0) {
            throw new BizException("非法分页参数");
        }
        int from = offset + 1;
        int to = offset + size;
        return "SELECT * FROM ("
                + "SELECT " + innerSelectColumns + ", ROW_NUMBER() OVER (ORDER BY " + orderBy + ") AS rn "
                + fromWhere
                + ") t WHERE t.rn BETWEEN " + from + " AND " + to;
    }
}
