package com.ruoyi.framework.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充字段处理器
 * 用于自动填充 createTime、updateTime 等字段
 */
public class CreateAndUpdateMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充字段
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充创建时间（字段名为 createTime）
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 填充更新时间（字段名为 updateTime）
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充字段
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充更新时间（字段名为 updateTime）
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}