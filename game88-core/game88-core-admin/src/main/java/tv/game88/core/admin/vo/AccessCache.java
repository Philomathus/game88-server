package tv.game88.core.admin.vo;

import lombok.Data;

@Data
public class AccessCache {
    /**
     * 一个计数周期内第一次访问的时间戳
     */
    private long firstVisitTimestamp;
    /**
     * 访问次数统计
     */
    private int accessCount;
}
