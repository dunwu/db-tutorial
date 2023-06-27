package io.github.dunwu.javadb.redis.jedis.rank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 排行榜分区信息实体
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2022-05-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RankRegion {

    /** 排行榜分区号 */
    private Integer regionNo;
    /** 排行榜分区 Redis Key */
    private String regionKey;
    /** 分区实际大小 */
    private Long size;
    /** 分区最大大小 */
    private Long maxSize;
    /** 分区中的最小值 */
    private RankRegionElement min;
    /** 分区中的最大值 */
    private RankRegionElement max;

    public RankRegion(Integer regionNo, String regionKey, Long size, Long maxSize) {
        this.regionNo = regionNo;
        this.regionKey = regionKey;
        this.size = size;
        this.maxSize = maxSize;
    }

}
