package io.github.dunwu.javadb;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * @author Zhang Peng
 */
public class JedisDemoTest {
    private static final String REDIS_HOST = "192.168.58.170";
    private static final int REDIS_PORT = 6379;
    private static final String REDIS_PASSWORD = "zp";
    private static Jedis jedis = null;
    private static Logger logger = LoggerFactory.getLogger(JedisDemoTest.class);

    @BeforeClass
    public static void beforeClass() {
        jedis = new Jedis(REDIS_HOST, REDIS_PORT);
        jedis.auth(REDIS_PASSWORD);
        System.out.println("ping redis: " + jedis.ping());
    }

    @Test
    public void testSet() {
        jedis.set("first", "hello world");
        System.out.println("first:" + jedis.get("first"));
        logger.debug("first: {}", jedis.get("first"));
    }

    @Test
    public void testLpush() {
        // 存储数据到列表中
        jedis.lpush("colors", "Red");
        jedis.lpush("colors", "Yellow");
        jedis.lpush("colors", "Blue");
        // 获取存储的数据并输出
        List<String> list = jedis.lrange("colors", 0, 2);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("列表项为: " + list.get(i));
        }
    }

    @Test
    public void testKeys() {
        // 存储数据到列表中
        Set<String> keys = jedis.keys("*");
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            System.out.println(key);
        }
    }
}
