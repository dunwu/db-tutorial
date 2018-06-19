package io.github.dunwu.javadb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Zhang Peng
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext.xml" })
public class JedisPoolDemoTest {
    private static Logger logger = LoggerFactory.getLogger(JedisPoolDemoTest.class);

    @Autowired
    private JedisPool jedisPool;

    @Test
    public void testSet() {
        Jedis jedis = jedisPool.getResource();
        jedis.set("first", "hello world");
        System.out.println("first:" + jedis.get("first"));
        logger.debug("first: {}", jedis.get("first"));
        jedis.close();
    }

    @Test
    public void testLpush() {
        Jedis jedis = jedisPool.getResource();

        // 存储数据到列表中
        jedis.lpush("colors", "Red");
        jedis.lpush("colors", "Yellow");
        jedis.lpush("colors", "Blue");
        // 获取存储的数据并输出
        List<String> list = jedis.lrange("colors", 0, 2);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("列表项为: " + list.get(i));
        }

        jedis.close();
    }

    @Test
    public void testKeys() {
        Jedis jedis = jedisPool.getResource();

        // 存储数据到列表中
        Set<String> keys = jedis.keys("*");
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            System.out.println(key);
        }

        jedis.close();
    }
}
