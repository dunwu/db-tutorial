package io.github.dunwu.javadb.redis.jedis.rank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排行榜（分区）元素信息
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2022-05-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankRegionElement {

    /** 排行榜分区号 */
    private Integer regionNo;
    /** 排行榜分区 Redis Key */
    private String regionKey;
    /** zset member */
    private String member;
    /** zset score */
    private Double score;
    /** 当前分区的排名 */
    private Long rank;
    /** 总排名 */
    private Long totalRank;

}
