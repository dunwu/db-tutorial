package io.github.dunwu.javadb.redis.jedis.rank;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 利用 sorted set 实现排行榜示例
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2022-05-26
 */
@Slf4j
public class RankDemo {

    public static final boolean isRegionRankEnabled = true;
    private final Jedis jedis;

    public RankDemo(Jedis jedis) {
        this.jedis = jedis;
    }

    // ================================================================================
    // 排行榜公共常量、方法
    // ================================================================================

    /**
     * 第一名
     */
    static final int FIRST = 0;
    /**
     * 头部排行榜长度
     */
    static final int HEAD_RANK_LENGTH = 200;
    /**
     * 总排行榜长度
     */
    static final long TOTAL_RANK_LENGTH = 1000;
    /**
     * 排行榜第一个分区长度
     */
    static final int FIRST_REGION_LEN = 1;
    /**
     * 普通分区长度
     */
    static final int COMMON_REGION_LEN = 50;
    /**
     * 排行榜最后一名位置
     */
    static final long RANK_END_OFFSET = -TOTAL_RANK_LENGTH - 1;

    /**
     * 根据 member，查询成员在排行榜中的排名，从 0 开始计数
     * <p>
     * 如果成员不在排行榜，则统一返回 {@link #TOTAL_RANK_LENGTH}
     *
     * @param member zset 成员
     * @return /
     */
    public RankElement getRankByMember(String member) {
        if (isRegionRankEnabled) {
            RankRegionElement element = getRankByMemberWithRegions(member);
            return BeanUtil.toBean(element, RankElement.class);
        } else {
            // 排行榜采用不分区方案
            return getRankByMemberWithNoRegions(member);
        }
    }

    /**
     * 根据从总排名的范围获取元素列表
     *
     * @param begin 总排名中的起始位置
     * @param end 总排名中的结束位置
     * @param isAsc true：从低到高 / false：从高到低
     * @return /
     */
    public List<RankElement> getRankElementList(long begin, long end, boolean isAsc) {

        if (begin < 0 || end >= TOTAL_RANK_LENGTH) {
            log.error("【排行榜】请求范围 begin = {}, end = {} 超出排行榜实际范围", begin, end);
            return null;
        }

        if (isRegionRankEnabled) {
            // 排行榜采用分区方案
            List<RankRegionElement> elementList = getRankElementListWithRegions(begin, end, isAsc);
            if (CollectionUtil.isEmpty(elementList)) {
                return null;
            }
            return elementList.stream().map(i -> BeanUtil.toBean(i, RankElement.class)).collect(Collectors.toList());
        } else {
            // 排行榜采用不分区方案
            return getRankElementListWithNoRegions(begin, end, isAsc);
        }
    }

    /**
     * 更新排行榜
     *
     * @param member 榜单成员
     * @param score 榜单成员分值
     */
    public void saveRank(String member, double score) {
        if (isRegionRankEnabled) {
            // 排行榜采用分区方案
            saveRankWithRegions(member, score);
        } else {
            // 排行榜采用不分区方案
            saveRankWithNoRegions(member, score);
        }
    }


    // ================================================================================
    // 排行榜【不分区】方案
    // ================================================================================

    /**
     * 排行榜缓存前缀
     */
    static final String RANK = "rank";

    /**
     * 根据 member，查询成员在排行榜中的排名，从 0 开始计数
     * <p>
     * 如果成员不在排行榜，则统一返回 {@link #TOTAL_RANK_LENGTH}
     *
     * @param member zset 成员
     * @return /
     */
    public RankElement getRankByMemberWithNoRegions(String member) {
        Long rank = jedis.zrevrank(RANK, member);
        if (rank != null) {
            Set<Tuple> tuples = jedis.zrevrangeWithScores(RANK, rank, rank);
            for (Tuple tuple : tuples) {
                if (tuple.getElement().equals(member)) {
                    return new RankElement(member, tuple.getScore(), rank);
                }
            }
        }
        return new RankElement(member, null, TOTAL_RANK_LENGTH);
    }

    /**
     * 根据从总排名的范围获取元素列表
     *
     * @param begin 总排名中的起始位置
     * @param end 总排名中的结束位置
     * @param isAsc true：从低到高 / false：从高到低
     * @return /
     */
    private List<RankElement> getRankElementListWithNoRegions(long begin, long end, boolean isAsc) {
        Set<Tuple> tuples;
        if (isAsc) {
            tuples = jedis.zrevrangeWithScores(RANK, begin, end);
        } else {
            tuples = jedis.zrangeWithScores(RANK, begin, end);
        }

        if (CollectionUtil.isEmpty(tuples)) {
            return null;
        }

        long rank = 0;
        List<RankElement> list = new ArrayList<>();
        for (Tuple tuple : tuples) {
            RankElement elementVo = new RankElement(tuple.getElement(), tuple.getScore(), rank++);
            list.add(elementVo);
        }
        return list;
    }

    /**
     * 更新【不分区】排行榜
     *
     * @param member 榜单成员
     * @param score 榜单成员分值
     */
    private void saveRankWithNoRegions(final String member, final double score) {
        Pipeline pipeline = jedis.pipelined();
        pipeline.zadd(RANK, score, member);
        pipeline.zremrangeByRank(RANK, 0, RANK_END_OFFSET);
        pipeline.sync();
    }


    // ================================================================================
    // 排行榜【分区】方案
    // ================================================================================

    /**
     * 排行榜缓存前缀
     */
    static final String RANK_PREFIX = "rank:";
    /**
     * 排行榜所有分区的分区号（分区号实际上就是该分区排名第一元素的实际排名）
     */
    static final List<RankRegion> REGIONS = getAllRankRegions();

    /**
     * 根据 member，查询成员在排行榜中的排名，从 0 开始计数
     * <p>
     * 如果成员不在排行榜，则统一返回 {@link #TOTAL_RANK_LENGTH}
     *
     * @param member zset 成员
     * @return /
     */
    public RankRegionElement getRankByMemberWithRegions(String member) {
        long totalRank = TOTAL_RANK_LENGTH;
        for (RankRegion region : REGIONS) {
            // 计算排行榜分区的 Redis Key
            Long rank = jedis.zrevrank(region.getRegionKey(), member);

            if (rank != null) {
                totalRank = getTotalRank(region.getRegionNo(), rank);
                return new RankRegionElement(region.getRegionNo(), region.getRegionKey(), member, null, rank,
                                             totalRank);
            }
        }
        int lastRegionNo = getLastRegionNo();
        return new RankRegionElement(lastRegionNo, getRankRedisKey(lastRegionNo), member, null, null, totalRank);
    }

    /**
     * 根据从总排名的范围获取元素列表
     *
     * @param begin 总排名中的起始位置
     * @param end 总排名中的结束位置
     * @param isAsc true：从低到高 / false：从高到低
     * @return /
     */
    public List<RankRegionElement> getRankElementListWithRegions(long begin, long end, boolean isAsc) {
        if (begin < 0 || end >= TOTAL_RANK_LENGTH) {
            log.error("【排行榜】请求范围 begin = {}, end = {} 超出排行榜实际范围", begin, end);
            return null;
        }

        List<RankRegionElement> finalList = new LinkedList<>();
        for (RankRegion region : REGIONS) {

            long regionBegin = region.getRegionNo();
            long regionEnd = region.getRegionNo() + region.getMaxSize() - 1;

            if (regionBegin > end) {
                break;
            }

            if (regionEnd < begin) {
                continue;
            }

            long first = Math.max(regionBegin, begin);
            long last = Math.min(regionEnd, end);
            RankRegionElement firstElement = getRegionRank(first);
            RankRegionElement lastElement = getRegionRank(last);
            List<RankRegionElement> list = getRankElementListInRegion(region, firstElement.getRank(),
                                                                      lastElement.getRank(), isAsc);
            if (CollectionUtil.isNotEmpty(list)) {
                finalList.addAll(list);
            }
        }
        return finalList;
    }

    /**
     * 获取指定分区中指定排名范围的信息
     *
     * @param region 指定榜单分区
     * @param begin 起始排名
     * @param end 结束排名
     * @param isAsc true：从低到高 / false：从高到低
     * @return 匹配排名的信息
     */
    private List<RankRegionElement> getRankElementListInRegion(RankRegion region, long begin, long end, boolean isAsc) {
        Set<Tuple> tuples;
        if (isAsc) {
            // 从低到高排名
            tuples = jedis.zrangeWithScores(region.getRegionKey(), begin, end);
        } else {
            // 从高到低排名
            tuples = jedis.zrevrangeWithScores(region.getRegionKey(), begin, end);
        }

        if (CollectionUtil.isEmpty(tuples)) {
            return null;
        }

        long regionRank = 0;
        List<RankRegionElement> list = new ArrayList<>();
        for (Tuple tuple : tuples) {
            long totalRank = getTotalRank(region.getRegionNo(), regionRank);
            RankRegionElement rankElementVo = new RankRegionElement(region.getRegionNo(), region.getRegionKey(),
                                                                    tuple.getElement(), tuple.getScore(), regionRank,
                                                                    totalRank);
            list.add(rankElementVo);
            regionRank++;
        }
        return list;
    }

    /**
     * 获取指定分区中指定排名的信息
     *
     * @param region 指定榜单分区
     * @param rank 分区中的排名
     * @param isAsc true：从低到高 / false：从高到低
     * @return 匹配排名的信息
     */
    private RankRegionElement getRankElementInRegion(RankRegion region, long rank, boolean isAsc) {
        Set<Tuple> tuples;
        if (isAsc) {
            // 从低到高排名
            tuples = jedis.zrangeWithScores(region.getRegionKey(), rank, rank);
        } else {
            // 从高到低排名
            tuples = jedis.zrevrangeWithScores(region.getRegionKey(), rank, rank);
        }

        if (CollectionUtil.isEmpty(tuples)) {
            return null;
        }

        Tuple tuple = tuples.iterator().next();
        if (tuple == null) {
            return null;
        }

        long regionRank = rank;
        if (isAsc) {
            regionRank = region.getMaxSize() - 1;
        }

        long totalRank = getTotalRank(region.getRegionNo(), rank);
        return new RankRegionElement(region.getRegionNo(), region.getRegionKey(), tuple.getElement(), tuple.getScore(),
                                     regionRank, totalRank);
    }

    /**
     * 获取最后一名
     */
    private RankRegionElement getMinRankElementInRegion(RankRegion region) {
        return getRankElementInRegion(region, FIRST, true);
    }

    /**
     * 获取第一名
     */
    private RankRegionElement getMaxRankElementInRegion(RankRegion region) {
        return getRankElementInRegion(region, FIRST, false);
    }

    /**
     * 更新【分区】排行榜
     *
     * @param member 榜单成员
     * @param score 榜单成员分值
     */
    public void saveRankWithRegions(final String member, final double score) {

        List<RankRegion> regions = new LinkedList<>(REGIONS);

        // member 的原始排名
        RankRegionElement oldRank = null;
        for (RankRegion region : regions) {

            region.setSize(jedis.zcard(region.getRegionKey()));
            region.setMin(getMinRankElementInRegion(region));
            region.setMax(getMaxRankElementInRegion(region));

            // 查找 member 是否已经在榜单中
            Long rank = jedis.zrevrank(region.getRegionKey(), member);
            if (rank != null) {
                jedis.zrevrangeWithScores(region.getRegionKey(), rank, rank);
                oldRank = getRankElementInRegion(region, rank, false);
            }
        }

        Pipeline pipeline = jedis.pipelined();
        // 如果成员已入榜，并且无任何变化，无需任何修改
        if (oldRank != null) {
            if (oldRank.getMember().equals(member) && oldRank.getScore() == score) {
                log.info("【排行榜】member = {}, score = {} 值没有变化，无需任何修改", member, score);
                return;
            }

            // 成员已经在 10W 排行榜中，先将旧记录自适应删除
            if (oldRank.getTotalRank() < TOTAL_RANK_LENGTH) {
                log.info("【排行榜】member = {} 已入 TOP {}，rank = {}", member, TOTAL_RANK_LENGTH, oldRank);
                // 先将原始排名记录删除，并动态调整所有分区
                deleteWithAutoAdjust(oldRank, regions, pipeline);
            }
        }

        // 将成员的记录插入到合适的分区中，并自适应调整各分区
        addWithAutoAdjust(member, score, regions, pipeline);
        pipeline.syncAndReturnAll();

        long newRank = TOTAL_RANK_LENGTH;
        for (RankRegion region : regions) {
            Long rank = jedis.zrevrank(region.getRegionKey(), member);
            if (rank != null) {
                newRank = getTotalRank(region.getRegionNo(), rank);
                break;
            }
        }
        log.info("【排行榜】member = {}, score = {}, 排名：{}", member, score, newRank);

        if (oldRank != null && oldRank.getTotalRank() < HEAD_RANK_LENGTH && newRank >= HEAD_RANK_LENGTH) {
            log.info("【排行榜】member = {} 跌出 TOP {}，oldRank = {}, newRank = {}", member, HEAD_RANK_LENGTH, oldRank,
                     newRank);
        }
    }

    /**
     * 根据 member，score 将成员的记录插入到合适的分区中，如果没有合适的分区，说明在 10W 名以外，则不插入
     * <p>
     * 如果成员在 {@link #TOTAL_RANK_LENGTH} 以内排行榜，则返回真实排名；否则，则统一返回 {@link #TOTAL_RANK_LENGTH}
     *
     * @param member zset 成员
     * @param score 成员分值
     */
    private void addWithAutoAdjust(String member, double score, List<RankRegion> regions, Pipeline pipeline) {

        String insertedMember = member;
        double insertedScore = score;

        for (RankRegion region : regions) {

            // 判断分区长度
            if (region.getSize() < region.getMaxSize()) {
                // 如果分区中实际数据量小于分区最大长度，则直接将成员插入排行榜即可：
                // 由于排行榜是按照分值从高到低排序，各分区也是有序排列。
                // 分区没有满的情况下，不会创建新的分区，所以，此时必然是最后一个分区。
                pipeline.zadd(region.getRegionKey(), insertedScore, insertedMember);
                region.setSize(region.getSize() + 1);
                break;
            }

            // 当前分区不为空，取最后一名
            if (region.getMin() == null) {
                log.error("【排行榜】【删除老记录】key = {} 未找到最后一名数据！", region.getRegionKey());
                break;
            }

            // 待插入分值比分区最小值还小
            if (region.getMin().getScore() >= insertedScore) {
                continue;
            }

            // 待插入分值大于当前分区的最小值，当前分区即为合适插入的分区
            // 将待插入成员、分值写入
            pipeline.zadd(region.getRegionKey(), insertedScore, insertedMember);

            // 从本分区中移出最后一名
            pipeline.zrem(region.getRegionKey(), region.getMin().getMember());

            // 移入下一个分区
            insertedMember = region.getMin().getMember();
            insertedScore = region.getMin().getScore();
        }
    }

    /**
     * 先将原始排名记录从所属分区中删除，并动态调整之后的分区
     */
    private void deleteWithAutoAdjust(RankRegionElement oldRank, List<RankRegion> regions, Pipeline pipeline) {

        // 计算排行榜分区的 Redis Key
        pipeline.zrem(oldRank.getRegionKey(), oldRank.getMember());
        log.info("【排行榜】【删除老记录】删除原始记录：key = {}, member = {}", oldRank.getRegionKey(), oldRank.getMember());

        int prevRegionNo = oldRank.getRegionNo();
        RankRegion prevRegion = null;
        for (RankRegion region : regions) {

            // prevRegion 及之前的分区无需处理
            if (Objects.equals(region.getRegionNo(), prevRegionNo)) {
                prevRegion = region;
                continue;
            }
            if (region.getRegionNo() < oldRank.getRegionNo()) { continue; }

            // 当前分区如果为空，则无需调整，结束
            if (region.getSize() == null || region.getSize() == 0L) {
                log.info("【排行榜】【删除老记录】key = {} 数据为空，无需处理", region.getRegionKey());
                break;
            }

            // 当前分区不为空，取第一名
            if (region.getMax() == null) {
                log.error("【排行榜】【删除老记录】key = {} 未找到第一名数据！", region.getRegionKey());
                break;
            }

            if (prevRegion == null) {
                break;
            }

            // 从本分区中移出第一名
            pipeline.zrem(region.getRegionKey(), region.getMax().getMember());
            region.setSize(region.getSize() - 1);
            // 移入上一个分区
            pipeline.zadd(prevRegion.getRegionKey(), region.getMax().getScore(), region.getMax().getMember());
            prevRegion.setSize(prevRegion.getSize() + 1);
            // 替换上一分区 key
            prevRegion = region;
        }
    }

    /**
     * 获取排行榜所有分区
     * <p>
     * 排行榜存储 10W 条数据，分区规则为：
     * 第一个分区，以 0 开始，存储 100 条数据（因为 TOP 100 查询频率高，所以分区大小设小一点，提高查询速度）
     * 最后一个分区，以 95100 开始，存储 4900 条数据；
     * 其他分区，都存储 5000 条数据
     */
    private static List<RankRegion> getAllRankRegions() {
        List<RankRegion> regions = new ArrayList<>();
        RankRegion firstRegion = new RankRegion(FIRST, getRankRedisKey(FIRST), null, getRegionLength(FIRST));
        regions.add(firstRegion);
        for (int index = FIRST_REGION_LEN; index < TOTAL_RANK_LENGTH; index = index + COMMON_REGION_LEN) {
            RankRegion region = new RankRegion(index, getRankRedisKey(index), null, getRegionLength(index));
            regions.add(region);
        }
        return regions;
    }

    /**
     * 根据排行榜每个分区的第一个索引数字，获取该分区的长度
     * <p>
     * 分区大小的规则：
     * 第一个分区，以 0 开始，存储 100 条数据；
     * 最后一个分区，以 95100 开始，存储 4900 条数据；
     * 其他分区，都存储 5000 条数据
     *
     * @param region 分区第一条数据的索引
     * @return 分区的长度
     */
    private static long getRegionLength(int region) {
        final int LAST = (int) ((TOTAL_RANK_LENGTH - 1) / COMMON_REGION_LEN * COMMON_REGION_LEN + FIRST_REGION_LEN);
        switch (region) {
            case FIRST:
                return FIRST_REGION_LEN;
            case LAST:
                return COMMON_REGION_LEN - FIRST_REGION_LEN;
            default:
                return COMMON_REGION_LEN;
        }
    }

    /**
     * 根据分区和分区中的排名，返回总排名
     */
    private static long getTotalRank(long regionNo, long rank) {
        for (RankRegion region : REGIONS) {
            if (region.getRegionNo().longValue() == regionNo) {
                return regionNo + rank;
            }
        }
        // 如果分区不存在，则统一返回 TOTAL_RANK_LENGTH
        return TOTAL_RANK_LENGTH;
    }

    /**
     * 根据总排名，返回该排名应该所属的分区及分区中的排名信息
     */
    private static RankRegionElement getRegionRank(long totalRank) {

        if (totalRank < 0 || totalRank >= TOTAL_RANK_LENGTH) { return null; }

        long length = totalRank;
        for (RankRegion region : REGIONS) {
            if (region.getMaxSize() > length) {
                return new RankRegionElement(region.getRegionNo(), region.getRegionKey(), null, null, length,
                                             totalRank);
            } else {
                length -= region.getMaxSize();
            }
        }
        return null;
    }

    /**
     * 根据总排名，计算得出排名所属分区
     */
    private static int getRegionByTotalRank(long totalRank) {
        if (totalRank < FIRST_REGION_LEN) {
            return 0;
        }
        return (int) (totalRank / COMMON_REGION_LEN * COMMON_REGION_LEN + FIRST_REGION_LEN);
    }

    /**
     * 获取最后一个分区
     */
    private static int getLastRegionNo() {
        return (int) ((TOTAL_RANK_LENGTH / COMMON_REGION_LEN - 1) * COMMON_REGION_LEN + FIRST_REGION_LEN);
    }

    /**
     * 排行榜缓存 Key
     *
     * @param regionNo 该分区第一个元素的排名
     */
    private static String getRankRedisKey(long regionNo) {
        return RANK_PREFIX + regionNo;
    }

}
