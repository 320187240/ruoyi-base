package com.ruoyi.framework.aspectj;

import com.ruoyi.common.annotation.DataSource;
import com.ruoyi.common.enums.DataSourceType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.datasource.DynamicDataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 多数据源处理
 * 
 * @author ruoyi
 */
@Aspect
@Order(1)
@Component
public class DataSourceAspect {
    private static final Logger log = LoggerFactory.getLogger(DataSourceAspect.class);

    @Pointcut("@annotation(com.ruoyi.common.annotation.DataSource) || @within(com.ruoyi.common.annotation.DataSource)")
    public void dsPointCut() {
    }

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        DataSource dataSource = getDataSource(point);
        DataSourceType type = DataSourceType.MASTER; // 默认主库

        if (dataSource != null) {
            String dsValue = dataSource.value();
            type = resolveDataSourceType(dsValue);
            DynamicDataSourceContextHolder.setDataSourceType(type);
            log.debug("切面切换数据源: {}", type);
        }

        try {
            return point.proceed();
        } finally {
            DynamicDataSourceContextHolder.reset();
            log.trace("数据源上下文已清理");
        }
    }

    private DataSource getDataSource(ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        DataSource methodAnnotation = AnnotationUtils.findAnnotation(signature.getMethod(), DataSource.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return AnnotationUtils.findAnnotation(signature.getDeclaringType(), DataSource.class);
    }

    private DataSourceType resolveDataSourceType(String value) {
        if (StringUtils.isBlank(value)) {
            return DataSourceType.MASTER;
        }
        try {
            return DataSourceType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("无效的数据源配置: {}，回退到主库", value);
            return DataSourceType.MASTER;
        }
    }
}