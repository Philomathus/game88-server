package tv.game88.general.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tv.game88.common.vo.BaseEntity;

/**
 * 域名加密管理对象 agent_secure
 *
 * @author 77tv
 * @date 2021-04-01
 */
@Data
public class AgentSecure extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 代理号 */
    @TableId( type = IdType.INPUT )
    private String id;

    /** 公钥(不要显示在页面) */
    private String publickey;

    /** 私钥(不要显示在页面) */
    private String privatekey;

    /** 原始url */
    private String urls;

    /** 加密url */
    private String secureUrls;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("publickey", getPublickey())
            .append("privatekey", getPrivatekey())
            .append("urls", getUrls())
            .append("secureUrls", getSecureUrls())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
