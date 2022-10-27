package tv.game88.core.config.entity;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tv.game88.common.vo.BaseEntity;

import java.time.LocalDateTime;

@Data
public class ConfigSms extends BaseEntity {
    /**
     * 主键
     */
    private Long          id;
    /**
     * SMS名称
     */
    private String        name;
    /**
     * 服务商
     */
    private Integer       provider;
    /**
     * appKey
     */
    private String        appKey;
    /**
     * appAccess
     */
    private String        appAccess;
    /**
     * 地区
     */
    private String        region;
    /**
     * 签名
     */
    private String        signature;
    /**
     * 模板
     */
    private String        template;
    /**
     * smsSdkAppid
     */
    private String        smsSdkAppid;
    /**
     * 管理员账号
     */
    private String        identify;
    /**
     * 状态
     */
    private Integer       isEffect;
    /**
     * 节点
     */
    private String        endpoint;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 更新时间
     */
    private LocalDateTime createTime;
    /**
     * 更新者
     */
    private String        updator;
    /**
     * 创建者
     */
    private String        creator;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "id", getId() )
                .append( "name", getName() )
                .append( "provider", getProvider() )
                .append( "appKey", getAppKey() )
                .append( "appAccess", getAppAccess() )
                .append( "region", getRegion() )
                .append( "signature", getSignature() )
                .append( "template", getTemplate() )
                .append( "smsSdkAppid", getSmsSdkAppid() )
                .append( "identify", getIdentify() )
                .append( "isEffect", getIsEffect() )
                .append( "endpoint", getEndpoint() )
                .append( "updateTime", getUpdateTime() )
                .append( "createTime", getCreateTime() )
                .append( "updator", getUpdator() )
                .append( "creator", getCreator() )
                .toString();
    }
}
