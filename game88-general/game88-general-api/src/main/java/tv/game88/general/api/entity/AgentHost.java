package tv.game88.general.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.vo.BaseEntity;

@Data
@EqualsAndHashCode( callSuper = false )
public class AgentHost extends BaseEntity {
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

}
