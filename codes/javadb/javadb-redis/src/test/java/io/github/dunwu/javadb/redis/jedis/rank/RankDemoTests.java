package io.github.dunwu.javadb.redis.jedis.rank;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 测试 {@link RankDemo}
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2022-05-24
 */
@Slf4j
@DisplayName("使用 zset 维护分区的排行榜缓存")
public class RankDemoTests {

    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static Jedis jedis = null;
    private RankDemo rank;

    @BeforeAll
    public static void beforeClass() {
        // Jedis 有多种构造方法，这里选用最简单的一种情况
        jedis = new Jedis(REDIS_HOST, REDIS_PORT);

        // 触发 ping 命令
        try {
            jedis.ping();
            jedis.select(0);
            log.debug("jedis 连接成功。");
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void afterClass() {
        if (null != jedis) {
            jedis.close();
            log.debug("jedis 关闭连接。");
        }
    }

    @BeforeEach
    public void beforeEach() {
        rank = new RankDemo(jedis);
    }

    @Test
    @DisplayName("刷新 MOCK 数据")
    public void refreshMockData() {
        log.info("刷新 MOCK 数据");

        // 清理所有排行榜分区
        for (RankRegion region : RankDemo.REGIONS) {
            jedis.del(region.getRegionKey());
        }
        jedis.del(RankDemo.RANK);

        for (int i = 0; i < RankDemo.TOTAL_RANK_LENGTH; i++) {
            double score = RandomUtil.randomDouble(100.0, 10000.0);
            String member = StrUtil.format("id-{}", i);
            rank.saveRank(member, score);
        }
    }

    @Test
    @DisplayName("测试各分区最大值、最小值")
    public void getRankElementList() {
        List<RankElement> list = rank.getRankElementList(0, 99, false);
        System.out.println(JSONUtil.toJsonStr(list));
        Assertions.assertEquals(100, list.size());
    }

    @Test
    @DisplayName("添加新纪录")
    public void testAdd() {

        String member1 = StrUtil.format("id-{}", RankDemo.TOTAL_RANK_LENGTH + 1);
        rank.saveRank(member1, 20000.0);

        String member2 = StrUtil.format("id-{}", RankDemo.TOTAL_RANK_LENGTH + 2);
        rank.saveRank(member2, 1.0);

        RankElement rank1 = rank.getRankByMember(member1);
        RankElement rank2 = rank.getRankByMember(member2);
        Assertions.assertEquals(RankDemo.FIRST, rank1.getTotalRank());
        Assertions.assertNull(rank2);
    }


    @Nested
    @DisplayName("分区方案特殊测试")
    public class RegionTest {

        @Test
        @DisplayName("测试各分区长度")
        public void testRegionLength() {
            for (RankRegion region : RankDemo.REGIONS) {
                Long size = jedis.zcard(region.getRegionKey());
                log.info("【排行榜】redisKey = {}, count = {}", region.getRegionKey(), size);
                Assertions.assertEquals(region.getMaxSize(), size);
            }
        }

        @Test
        @DisplayName("测试各分区最大值、最小值")
        public void testRegionSort() {
            // 按序获取每个分区的最大值、最小值
            List<Double> maxScores = new LinkedList<>();
            List<Double> minScores = new LinkedList<>();
            for (RankRegion region : RankDemo.REGIONS) {
                Set<Tuple> minSet = jedis.zrangeWithScores(region.getRegionKey(), 0, 0);
                Tuple min = minSet.iterator().next();
                minScores.add(min.getScore());

                Set<Tuple> maxSet = jedis.zrevrangeWithScores(region.getRegionKey(), 0, 0);
                Tuple max = maxSet.iterator().next();
                maxScores.add(max.getScore());
            }
            System.out.println(maxScores);
            System.out.println(minScores);

            // 最大值、最小值数量必然相同
            Assertions.assertEquals(maxScores.size(), minScores.size());

            for (int i = 0; i < minScores.size(); i++) {
                compareMinScore(maxScores, i, minScores.get(i));
            }
        }

        public void compareMinScore(List<Double> maxScores, int region, double score) {
            for (int i = region + 1; i < maxScores.size(); i++) {
                Assertions.assertFalse(score <= maxScores.get(i),
                                       StrUtil.format("region = {}, score = {} 的最小值小于后续分区中的数值（region = {}, score = {}）",
                                                      region, score, i, maxScores.get(i)));
            }
        }

    }

}
