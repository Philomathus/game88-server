package tv.game88.general.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tv.game88.common.vo.BaseEntity;

/**
 * 代理管理对象 agent
 *
 * @author 77tv
 * @date 2021-04-16
 */
@Data
public class Agent extends BaseEntity {
    /** 代理key */
    @TableId( type = IdType.INPUT, value = "`key`")
    private String key;

    /** 公司名称 */
    private String name;

    /** 状态(1启用0停用) */
    private String status;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("name", getName())
            .append("key", getKey())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
