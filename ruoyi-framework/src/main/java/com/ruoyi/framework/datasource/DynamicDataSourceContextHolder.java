package com.ruoyi.framework.datasource;

import com.ruoyi.common.enums.DataSourceType;

/**
 * 数据源切换处理
 *
 * @author ruoyi
 */
public class DynamicDataSourceContextHolder {
    private static final ThreadLocal<DataSourceType> CONTEXT = ThreadLocal.withInitial(() -> DataSourceType.MASTER);

    public static void setDataSourceType(DataSourceType type) {
        CONTEXT.set(type);
    }

    public static String getDataSourceType() {
        return CONTEXT.get().name().toLowerCase();
    }

    public static void reset() {
        CONTEXT.remove();
    }

    public static void forceMaster() {
        CONTEXT.set(DataSourceType.MASTER);
    }
}