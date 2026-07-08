package com.steel.perf.integration.erp;

import com.steel.perf.common.exception.BizException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlServerDialectTest {

    @Test
    void paginate2008() {
        String sql = SqlServerDialect.paginate(
                "emp_id, name", "FROM dwd_sales_detail WHERE tenant_id = :t", "emp_id", 20, 10);
        assertEquals("SELECT * FROM (SELECT emp_id, name, ROW_NUMBER() OVER (ORDER BY emp_id) AS rn "
                + "FROM dwd_sales_detail WHERE tenant_id = :t) t WHERE t.rn BETWEEN 21 AND 30", sql);
    }

    @Test
    void requireOrderBy() {
        assertThrows(BizException.class, () ->
                SqlServerDialect.paginate("a", "FROM x", "", 0, 10));
    }
}
