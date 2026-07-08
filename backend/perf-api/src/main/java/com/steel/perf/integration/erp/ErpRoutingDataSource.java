package com.steel.perf.integration.erp;

import com.steel.perf.common.tenant.TenantContext;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * ERP 路由数据源：按当前租户上下文路由到该租户的 ERP 只读连接（未注册回退默认）。
 * 实现多租户"各自 SQL Server 只读"的取数隔离。
 */
public class ErpRoutingDataSource extends AbstractDataSource {

    private final ErpDataSourceRegistry registry;

    public ErpRoutingDataSource(ErpDataSourceRegistry registry) {
        this.registry = registry;
    }

    private DataSource current() {
        TenantContext.Ctx ctx = TenantContext.get();
        String tenantId = ctx == null ? null : ctx.tenantId();
        DataSource ds = registry.resolve(tenantId);
        if (ds == null) {
            throw new IllegalStateException("未配置 ERP 数据源（默认与租户均缺失）");
        }
        return ds;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return current().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return current().getConnection(username, password);
    }
}
