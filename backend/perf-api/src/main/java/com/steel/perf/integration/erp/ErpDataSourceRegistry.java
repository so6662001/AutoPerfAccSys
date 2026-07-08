package com.steel.perf.integration.erp;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ERP 只读数据源注册表：按租户维护各自的 SQL Server 只读连接。
 * 生产由租户服务读取 t_tenant.erp_conn_enc（解密）构建并注册；未注册租户回退默认数据源。
 */
@Component
public class ErpDataSourceRegistry {

    private final Map<String, DataSource> tenantDs = new ConcurrentHashMap<>();
    private volatile DataSource defaultDataSource;

    public void setDefault(DataSource ds) {
        this.defaultDataSource = ds;
    }

    public void register(String tenantId, DataSource ds) {
        tenantDs.put(tenantId, ds);
    }

    public DataSource resolve(String tenantId) {
        DataSource ds = tenantId == null ? null : tenantDs.get(tenantId);
        return ds != null ? ds : defaultDataSource;
    }
}
