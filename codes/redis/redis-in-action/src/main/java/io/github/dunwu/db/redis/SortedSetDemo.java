package io.github.dunwu.db.redis;

import cn.hutool.core.util.RandomUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.Set;

/**
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2022-05-20
 */
public class SortedSetDemo {

    public static final String TEST_KEY = "test:zset";
    public static final Jedis conn = new Jedis("localhost");

    public static void main(String[] args) {
        conn.select(0);
        // zadd(conn);
        zrem(conn);
        // zrank(conn);
        // zrange(conn);
        zcard(conn);
        conn.close();
    }

    public static void zadd(Jedis conn) {
        for (int i = 0; i < 100; i++) {
            conn.zadd(TEST_KEY, RandomUtil.randomDouble(10000.0), RandomUtil.randomString(6));
        }
        conn.zadd(TEST_KEY, 20000.0, "THETOP");
    }

    public static void zrem(Jedis conn) {
        int len = 10;
        int end = -len - 1;
        conn.zremrangeByRank(TEST_KEY, 0, end);
    }

    public static void zcard(Jedis conn) {
        System.out.println("count = " + conn.zcard(TEST_KEY));
    }

    public static void zrank(Jedis conn) {
        System.out.println("THETOP 从低到高排名：" + conn.zrank(TEST_KEY, "THETOP"));
        System.out.println("THETOP 从高到低排名：" + conn.zrevrank(TEST_KEY, "THETOP"));
    }

    public static void zrange(Jedis conn) {
        System.out.println("查看从低到高第 1 名：" + conn.zrange(TEST_KEY, 0, 0));
        System.out.println("查看从高到低第 1 名：" + conn.zrevrange(TEST_KEY, 0, 0));
        System.out.println("查看从高到低前 10 名：" + conn.zrevrange(TEST_KEY, 0, 9));
        Set<Tuple> tuples = conn.zrevrangeWithScores(TEST_KEY, 0, 0);
        for (Tuple tuple : tuples) {
            System.out.println(tuple.getElement());
            System.out.println(tuple.getScore());
        }

        System.out.println("查看从高到低前 10 名：" + conn.zrevrangeWithScores(TEST_KEY, 0, 0));
    }

}
