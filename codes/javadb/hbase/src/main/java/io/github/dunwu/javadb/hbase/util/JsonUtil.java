package io.github.dunwu.javadb.hbase.util;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtil {

    private JsonUtil() { }

    private static final ObjectMapper OBJECT_MAPPER;
    private static final TypeFactory TYPE_FACTORY;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TYPE_FACTORY = OBJECT_MAPPER.getTypeFactory();
    }

    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }

    /**
     * 简单对象转换
     */
    @SuppressWarnings("unchecked")
    public static <T> T toBean(String json, Class<T> clazz) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        if (clazz == String.class) {
            return (T) json;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("反序列化失败！json: {}, msg: {}", json, e.getMessage());
        }
        return null;
    }

    /**
     * 复杂对象转换
     */
    public static <T> T toBean(String json, TypeReference<T> typeReference) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        try {
            return (T) OBJECT_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("反序列化失败！json: {}, msg: {}", json, e.getMessage());
        }
        return null;
    }

    public static <T, K, V> T toBean(Map<K, V> map, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(toString(map), clazz);
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化失败！obj: {}, msg: {}", obj, e.getMessage());
        }
        return null;
    }

    public static <K, V> Map<K, V> toMap(String json) {
        if (StrUtil.isBlank(json)) {
            return new HashMap<>(0);
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<K, V>>() { });
        } catch (Exception e) {
            log.error("反序列化失败！json: {}, msg: {}", json, e.getMessage());
        }
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> toMap(Object obj) {

        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(toString(obj), new TypeReference<Map<K, V>>() { });
        } catch (IOException e) {
            log.error("反序列化失败！json: {}, msg: {}", toString(obj), e.getMessage());
        }
        return null;
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        JavaType javaType = TYPE_FACTORY.constructParametricType(List.class, clazz);
        try {
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            log.error("反序列化失败！json: {}, msg: {}", json, e.getMessage());
        }
        return null;
    }

    public static <T> List<T> toList(String json, TypeReference<T> typeReference) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        JavaType elementType = TYPE_FACTORY.constructType(typeReference);
        JavaType javaType = TYPE_FACTORY.constructParametricType(List.class, elementType);
        try {
            return OBJECT_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            log.error("反序列化失败！json: {}, msg: {}", json, e.getMessage());
        }
        return null;
    }

}