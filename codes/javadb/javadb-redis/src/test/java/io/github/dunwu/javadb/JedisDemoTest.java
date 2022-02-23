package io.github.dunwu.javadb;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Jedis 测试例
 * @author Zhang Peng
 * @see https://github.com/xetorthio/jedis
 */
public class JedisDemoTest {

    private static final String REDIS_HOST = "localhost";

    private static final int REDIS_PORT = 6379;

    private static Jedis jedis = null;

    private static Logger logger = LoggerFactory.getLogger(JedisDemoTest.class);

    @BeforeAll
    public static void beforeClass() {
        // Jedis 有多种构造方法，这里选用最简单的一种情况
        jedis = new Jedis(REDIS_HOST, REDIS_PORT);

        // 触发 ping 命令
        try {
            jedis.ping();
            logger.debug("jedis 连接成功。");
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void afterClass() {
        if (null != jedis) {
            jedis.close();
            logger.debug("jedis 关闭连接。");
        }
    }

    /**
     * 增删改 string 类型
     */
    @Test
    public void testString() {
        final String key = "word";
        final String value1 = "first";
        final String value2 = "second";

        // 新增 key
        jedis.set(key, value1);
        Assertions.assertEquals(value1, jedis.get(key));

        // 修改 key
        jedis.set(key, value2);
        Assertions.assertEquals(value2, jedis.get(key));

        Assertions.assertEquals(true, jedis.exists(key));

        // 删除 key
        jedis.del(key);
        Assertions.assertEquals(null, jedis.get(key));
        Assertions.assertEquals(false, jedis.exists(key));
    }

    /**
     * 增删改 byte[] 类型（本质也是 string 类型）
     */
    @Test
    public void testBytes() {
        final byte[] key = "word".getBytes();
        final byte[] value1 = "first".getBytes();
        final byte[] value2 = "second".getBytes();

        // 新增 key
        jedis.set(key, value1);
        Assertions.assertArrayEquals(value1, jedis.get(key));

        // 修改 key
        jedis.set(key, value2);
        Assertions.assertArrayEquals(value2, jedis.get(key));

        // 删除 key
        jedis.del(key);
        Assertions.assertArrayEquals(null, jedis.get(key));
    }

    /**
     * 增删改 Hash 类型
     */
    @Test
    public void testHash() {
        final String key = "zpkey";
        final String field1 = "first";
        final String value1 = "一";
        final String value1_1 = "1";
        final String field2 = "second";
        final String value2 = "二";

        // 新增 field
        jedis.hset(key, field1, value1);
        jedis.hset(key, field2, value2);
        Assertions.assertEquals(value1, jedis.hget(key, field1));
        Assertions.assertEquals(value2, jedis.hget(key, field2));

        // 修改 field
        jedis.hset(key, field1, value1_1);
        Assertions.assertEquals(value1_1, jedis.hget(key, field1));

        jedis.hdel(key, field1, value1_1);
        Assertions.assertEquals(null, jedis.hget(key, field1));

        Assertions.assertEquals(false, jedis.hexists(key, field1));
        Assertions.assertEquals(true, jedis.hexists(key, field2));

        Map<String, String> results = jedis.hgetAll(key);
        Assertions.assertEquals(1, results.size());
    }

    /**
     * set & get 命令
     */
    @Test
    public void testList() {
        final String key = "colors";
        // 存储数据到列表中
        jedis.lpush(key, "Red");
        jedis.lpush(key, "Yellow");
        jedis.lpush(key, "Blue");
        Assertions.assertEquals(3L, jedis.llen(key).longValue());

        // 获取存储的数据并输出
        List<String> list = jedis.lrange("colors", 0, 2);
        for (String aList : list) {
            System.out.println("列表项为: " + aList);
        }
    }

    @Test
    public void testKeys() {
        // 存储数据到列表中
        Set<String> keys = jedis.keys("*");
        for (String key : keys) {
            System.out.println(key);
        }
    }

}
