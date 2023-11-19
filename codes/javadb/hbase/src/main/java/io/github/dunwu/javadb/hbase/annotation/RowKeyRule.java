package io.github.dunwu.javadb.hbase.annotation;

import io.github.dunwu.javadb.hbase.constant.RowType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表主键标识
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-11-17
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
public @interface RowKeyRule {

    /**
     * 字段名（该值可无）
     */
    String value() default "";

    /**
     * 主键类型 {@link RowType}
     */
    RowType type() default RowType.ORIGIN_ID;

    /**
     * 原 ID 长度，type 为 {@link RowType#ORIGIN_ID} 或 {@link RowType#BUCKET} 时必填
     */
    int length() default 0;

    /**
     * 分桶数，type 为 {@link RowType#BUCKET} 时，才需要且必须指定
     */
    int bucket() default 0;

}
