package io.github.dunwu.javadb.redis.jedis.rank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排行榜元素信息
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2022-05-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankElement {

    /** zset member */
    private String member;
    /** zset score */
    private Double score;
    /** 总排名 */
    private Long totalRank;

}
