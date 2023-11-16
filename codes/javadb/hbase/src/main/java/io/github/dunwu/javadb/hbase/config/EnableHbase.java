package io.github.dunwu.javadb.hbase.config;

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启动 HBase 配置注解
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-06-30
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@EnableAspectJAutoProxy(
    proxyTargetClass = false
)
@Import({ HbaseConfiguration.class })
@Documented
public @interface EnableHbase {
}
