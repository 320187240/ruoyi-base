package com.ruoyi.common.enums;

/**
 * 数据源
 * 
 * @author ruoyi
 */
public enum DataSourceType {
    MASTER, SLAVE;

    public static DataSourceType fromString(String value) {
        if (value == null) {
            return MASTER;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return MASTER;
        }
    }
}