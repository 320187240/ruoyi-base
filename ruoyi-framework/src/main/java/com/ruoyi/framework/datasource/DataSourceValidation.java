package com.ruoyi.framework.datasource;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

@Configuration
public class DataSourceValidation {
    @Autowired
    @Qualifier("dynamicDataSource")
    private DataSource dataSource;

    @PostConstruct
    public void validate() {
        AbstractRoutingDataSource ards = (AbstractRoutingDataSource) dataSource;
        System.out.println("默认数据源: " + ards.getResolvedDataSources());
        System.out.println("目标数据源: " + ards.getResolvedDataSources());
    }
}