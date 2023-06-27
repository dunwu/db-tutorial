package io.github.dunwu.javadb.elasticsearch.springboot.constant;

/**
 * 关键字命名策略枚举
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2019-12-18
 */
public enum NamingStrategy {
    /**
     * 默认命名
     */
    DEFAULT,
    /**
     * 驼峰命名。例：namingStrategy
     */
    CAMEL,
    /**
     * 全小写字母用下划线拼接。例：naming_strategy
     */
    LOWER_UNDERLINE,
    /**
     * 全大写字母用下划线拼接。例：NAMING_STRATEGY
     */
    UPPER_UNDERLINE,
    /**
     * 全小写字母用分割线拼接。例：naming-strategy
     */
    LOWER_DASHED,
    /**
     * 全小写字母用分割线拼接。例：NAMING-STRATEGY
     */
    UPPER_DASHED,
}
