package tv.game88.platform.api.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ActivityType {
    /**
     * 系统编号
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}