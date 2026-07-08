package com.steel.perf.integration;

import com.steel.perf.integration.erp.ErpDataSourceRegistry;
import com.steel.perf.integration.erp.ErpReader;
import com.steel.perf.integration.erp.ErpRoutingDataSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * ERP 只读取数实现（NamedParameterJdbcTemplate，参数绑定）。
 * 通过 {@link ErpRoutingDataSource} 按当前租户路由到各自 ERP 只读连接；
 * dev/演示以平台数据源作默认回退，生产各租户注册独立 SQL Server 只读连接。
 * 内部持有路由数据源（非 Spring DataSource Bean），避免与平台数据源产生自动装配歧义。
 */
@Component
public class JdbcErpReader implements ErpReader {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcErpReader(DataSource platformDataSource, ErpDataSourceRegistry registry) {
        registry.setDefault(platformDataSource); // 默认回退到平台库（dev/演示）
        this.jdbc = new NamedParameterJdbcTemplate(new ErpRoutingDataSource(registry));
        this.jdbc.getJdbcTemplate().setMaxRows(200000);
        this.jdbc.getJdbcTemplate().setQueryTimeout(30);
    }

    @Override
    public List<Map<String, Object>> query(String sql, Map<String, Object> params) {
        // 仅执行由 SafeSqlCompiler 生成的参数化 SELECT；值全部命名参数绑定。
        return jdbc.queryForList(sql, params);
    }
}
