package com.steel.perf.integration;

import com.steel.perf.integration.erp.ErpReader;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * ERP 只读取数实现（NamedParameterJdbcTemplate，参数绑定）。
 * <p>M-dev：默认绑定到平台数据源作演示；生产应注入独立的 <b>ERP 只读数据源</b>
 * （SQL Server 2008+，只读账号 + 查询超时 + 行数上限，按租户路由）。
 */
@Component
public class JdbcErpReader implements ErpReader {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcErpReader(DataSource dataSource) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
        // 生产环境：this.jdbc.getJdbcTemplate().setMaxRows(200000); setQueryTimeout(30);
        this.jdbc.getJdbcTemplate().setMaxRows(200000);
        this.jdbc.getJdbcTemplate().setQueryTimeout(30);
    }

    @Override
    public List<Map<String, Object>> query(String sql, Map<String, Object> params) {
        // 仅执行由 SafeSqlCompiler 生成的参数化 SELECT；值全部命名参数绑定。
        return jdbc.queryForList(sql, params);
    }
}
