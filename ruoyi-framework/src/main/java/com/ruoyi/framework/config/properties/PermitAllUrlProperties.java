package com.ruoyi.framework.config.properties;

import com.ruoyi.common.annotation.Anonymous;
import org.apache.commons.lang3.RegExUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PatternParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 扫描 @Anonymous 注解
 * 在应用启动时，自动扫描所有 Controller 方法和类上的 @Anonymous 注解。
 * 将标注了该注解的接口路径标记为允许匿名访问。
 * 路径模式转换
 * 将路径参数（如 /user/{id}）转换为 Spring Security 兼容的通配符模式（如 /user/**）。
 * 通过正则表达式将 {参数} 替换为 **，以匹配多级路径。
 * 路径合法性校验
 * 使用 PathPatternParser 验证生成的路径模式是否符合 Spring 的路径匹配规范。
 * 过滤掉可能导致 PatternParseException 的非法路径（如连续通配符或非法组合）。
 * 提供 URL 列表
 * 将合法的匿名访问路径存储在 List<String> urls 中，供 Spring Security 配置使用。
 */
@Configuration
public class PermitAllUrlProperties implements InitializingBean, ApplicationContextAware {


    private static final Logger logger = LoggerFactory.getLogger(PermitAllUrlProperties.class);
    private static final Pattern PATTERN = Pattern.compile("\\{.*?\\}");
    private static final String ASTERISK = "**";
    private ApplicationContext applicationContext;
    private List<String> urls = new ArrayList<>();

    @Override
    public void afterPropertiesSet() {
        logger.debug("开始扫描允许匿名访问的URL...");
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();

        map.forEach((info, handlerMethod) -> {
            // 处理方法上的 @Anonymous 注解
            Anonymous methodAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), Anonymous.class);
            processPatterns(info, methodAnnotation, handlerMethod);
            // 处理类上的 @Anonymous 注解
            Anonymous classAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), Anonymous.class);
            processPatterns(info, classAnnotation, handlerMethod);
        });
    }

    private void processPatterns(RequestMappingInfo info, Anonymous annotation, HandlerMethod handlerMethod) {
        // 1. 获取 PathPatternsRequestCondition（Spring Boot 3 默认使用新路径解析器）
        PathPatternsRequestCondition pathPatternsCondition = info.getPathPatternsCondition();

        // 2. 空安全检查
        if (annotation == null || pathPatternsCondition == null) {
            return;
        }

        // 3. 获取路径模式集合（Spring 保证非空）
        Set<PathPattern> pathPatterns = pathPatternsCondition.getPatterns();

        // 4. 处理每个路径模式
        pathPatterns.forEach(pathPattern -> {
            // 4.1 获取原始路径字符串（如 "/user/{id}"）
            String originalPattern = pathPattern.getPatternString();
            // 4.2 将路径参数替换为 **（如 "/user/{id}" → "/user/**"）
            String convertedPattern = RegExUtils.replaceAll(originalPattern, PATTERN, "**");
            // 4.3 验证路径模式（可选，如果 Security 使用 Ant 匹配器则不需要）
            try {
                new PathPatternParser().parse(convertedPattern);
                urls.add(convertedPattern);
                logger.debug("转换路径: {} → {}", originalPattern, convertedPattern);
            } catch (PatternParseException e) {
                logger.error("非法路径模式: {}", convertedPattern, e);
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}