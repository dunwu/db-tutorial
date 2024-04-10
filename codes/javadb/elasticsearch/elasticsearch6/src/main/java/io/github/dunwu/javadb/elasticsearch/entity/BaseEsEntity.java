package io.github.dunwu.javadb.elasticsearch.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ES 实体接口
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @since 2023-06-28
 */
@Data
@ToString
public abstract class BaseEsEntity implements Serializable {

    public static final String DOC_ID = "docId";

    /**
     * 获取版本
     */
    protected Long version;

    protected Float hitScore;

    public abstract String getDocId();

    public static Map<String, String> getPropertiesMap() {
        Map<String, String> map = new LinkedHashMap<>(1);
        map.put(BaseEsEntity.DOC_ID, "keyword");
        return map;
    }

}
