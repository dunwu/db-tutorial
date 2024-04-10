package io.github.dunwu.javadb.elasticsearch.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 短剧、长视频消费数据 ES 实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2024-04-02
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEsEntity implements Serializable {

    private String id;
    private String name;
    private Integer age;

    @Override
    public String getDocId() {
        return id;
    }

    public static Map<String, String> getPropertiesMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(BaseEsEntity.DOC_ID, "keyword");
        map.put("id", "long");
        map.put("name", "keyword");
        map.put("age", "integer");
        return map;
    }

}
