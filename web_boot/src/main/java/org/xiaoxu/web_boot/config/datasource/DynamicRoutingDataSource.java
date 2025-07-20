package org.xiaoxu.web_boot.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Spring 每次获取连接时会调用 determineCurrentLookupKey()，你只需要让它返回当前线程的 key 即可。
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceKey();
    }
}
