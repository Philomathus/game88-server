package tv.game88.core.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;

import java.math.BigDecimal;

@Data
public class RspMember {
    @Schema( title = "登录令牌" )
    private String     token;
    @Schema( title = "会员ID" )
    private String     id;
    @Schema( title = "昵称" )
    private String     nickName;
    @Schema( title = "会员vip(vip等级)" )
    private Integer    vip;
    @Schema( title = "头像ID" )
    private String     headImg;
    @Schema( title = "余额" )
    private BigDecimal accountNow;
    @Schema( title = "充值总金额" )
    private BigDecimal accountCharge;
    @Schema( title = "累计有效打码" )
    private BigDecimal codeNow;
    @Schema( title = "累计需求打码（充值+优惠）" )
    private BigDecimal codeWill;
    @Schema( title = "累计注单" )
    private BigDecimal codeTotal;
    @Schema( title = "升级还需打码量" )
    private BigDecimal nextLevelIntegral = BigDecimal.ZERO;
    @Schema( title = "状态" )
    private Integer    status;
    @Schema( title = "邀请人" )
    private String     inviterCode;
    @Schema( title = "注册类型", description = "0游客 1会员" )
    private Integer    registerType;

    @Schema( title = "登录密码", hidden = true )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String password;

    public String getHeadImg() {
        if ( StringUtils.isNotBlank( headImg ) && !headImg.startsWith( "http" ) ) {
            return ConfigDomainCacheUtil.me.getDomainOssValue() + "/88lm/publicImage/head" + headImg + ".png";
        }
        return headImg;
    }
}
