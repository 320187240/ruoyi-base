package com.ruoyi.framework.datasource;

import com.ruoyi.common.enums.DataSourceType;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 动态数据源
 *
 * @author ruoyi
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    private final AtomicReference<DataSourceType> current = new AtomicReference<>(DataSourceType.MASTER);
    private DataSource masterDataSource; // 主库数据源，用于健康检查和重新创建
    private final DataSource slaveDataSource; // 从库数据源

    // 注入主库数据源的配置属性
    @Value("${spring.datasource.master.jdbc-url}")
    private String masterUrl;
    @Value("${spring.datasource.master.username}")
    private String masterUsername;
    @Value("${spring.datasource.master.password}")
    private String masterPassword;
    @Value("${spring.datasource.master.driver-class-name}")
    private String masterDriverClassName;

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void init() {
        // 在 Bean 初始化完成后启动健康检查任务
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this::checkMasterHealth, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * 构造函数，初始化主从数据源
     *
     * @param master 主库数据源
     * @param slave  从库数据源
     */
    public DynamicDataSource(DataSource master, DataSource slave) {
        this.masterDataSource = master;
        this.slaveDataSource = slave;
        super.setDefaultTargetDataSource(master);
        super.setTargetDataSources(Map.of(
                DataSourceType.MASTER, master,
                DataSourceType.SLAVE, slave
        ));
        super.afterPropertiesSet();
    }

    /**
     * 确定当前数据源的查找键
     *
     * @return 当前数据源类型
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return current.get();
    }

    /**
     * 定期检查主库的健康状态
     */
    private void checkMasterHealth() {
        try {
            // 检查主库数据源是否关闭
            if (masterDataSource instanceof HikariDataSource && ((HikariDataSource) masterDataSource).isClosed()) {
                log.error("主库数据源已关闭，尝试重新初始化");
                recreateMasterDataSource();
                return;
            }

            // 获取连接并检查有效性，设置5秒超时
            try (Connection conn = masterDataSource.getConnection()) {
                if (!conn.isValid(5)) {
                    current.set(DataSourceType.SLAVE);
                    log.warn("主库失效，切换到从库");
                } else {
                    current.set(DataSourceType.MASTER);
                    log.info("主库正常，切换回主库");
                }
            } catch (SQLException e) {
                current.set(DataSourceType.SLAVE);
                log.error("主库连接失败，自动切换到从库", e);
            }
        } catch (Exception e) {
            log.error("健康检查任务执行异常", e);
        }
    }

    /**
     * 重新创建主库数据源
     */
    private synchronized void recreateMasterDataSource() {
        try {
            // 创建新的主库数据源实例
            HikariDataSource newMaster = new HikariDataSource();
            newMaster.setJdbcUrl(masterUrl);
            newMaster.setUsername(masterUsername);
            newMaster.setPassword(masterPassword);
            newMaster.setDriverClassName(masterDriverClassName);

            // 配置 HikariCP 属性，与配置文件保持一致
            newMaster.setAutoCommit(true);
            newMaster.setConnectionTimeout(30000);
            newMaster.setIdleTimeout(600000);
            newMaster.setMaxLifetime(1800000);
            newMaster.setMaximumPoolSize(20);
            newMaster.setMinimumIdle(10);
            newMaster.setConnectionTestQuery("SELECT 1");

            // 更新动态数据源的目标数据源映射
            Map<Object, Object> targetDataSources = new HashMap<>();
            targetDataSources.put(DataSourceType.MASTER, newMaster);
            targetDataSources.put(DataSourceType.SLAVE, slaveDataSource);
            super.setTargetDataSources(targetDataSources);
            super.setDefaultTargetDataSource(newMaster);
            super.afterPropertiesSet();

            // 更新主库数据源引用
            this.masterDataSource = newMaster;

            log.info("主库数据源重新创建成功");
        } catch (Exception e) {
            log.error("重新创建主库数据源失败", e);
        }
    }
}