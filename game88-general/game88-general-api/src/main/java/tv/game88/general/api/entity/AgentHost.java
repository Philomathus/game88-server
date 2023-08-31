package tv.game88.general.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode( callSuper = false )
public class AgentHost {
    /**
     * 代理号
     */
    private String        id;
    /**
     * 直播名称
     */
    private String        name;
    /**
     * 接口地址
     */
    private String        apiUrl;
    /**
     * 状态(1启用0停用)
     */
    private Integer       status;
    /**
     * 邀请码
     */
    private String        code;
    /**
     * 创建人
     */
    private String        createBy;
    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    /**
     * 最后更新人
     */
    private String        updateBy;
    /**
     * 最后更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;

}
