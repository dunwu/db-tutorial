package io.github.dunwu.javadb.elasticsearch.util;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类
 *
 * @author Zhang Peng
 * @date 2023-06-29
 */
@Slf4j
public class JsonUtil {

    private static final ObjectMapper MAPPER =
        JsonMapper.builder()
                  .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                  .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                  .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                  .serializationInclusion(JsonInclude.Include.ALWAYS)
                  .build();

    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        JavaType javaType = MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        try {
            return MAPPER.readValue(json, javaType);
        } catch (Exception e) {
            log.error("反序列化失败！json: {}, msg: {}", json, e.getMessage());
        }
        return null;
    }

    public static <K, V> Map<K, V> toMap(String json) {
        if (StrUtil.isBlank(json)) {
            return new HashMap<>(0);
        }
        try {
            return MAPPER.readValue(json, new TypeReference<Map<K, V>>() { });
        } catch (Exception e) {
            log.error("反序列化失败！json: {}, msg: {}", json, e.getMessage());
        }
        return Collections.emptyMap();
    }

    public static <T> T toBean(String json, Class<T> clazz) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.error("反序列化失败！json: {}, msg: {}", json, e.getMessage());
        }
        return null;
    }

    public static <T> T toBean(String json, TypeReference<T> typeReference) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return (T) MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("反序列化失败！json: {}, msg: {}", json, e.getMessage());
        }
        return null;
    }

    public static <T> String toJson(T obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("序列化失败！obj: {}, msg: {}", obj, e.getMessage());
        }
        return null;
    }

}
