package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspMember {
    @Schema( name = "登录令牌" )
    private String     token;
    @Schema( name = "会员ID" )
    private String     id;
    @Schema( name = "昵称" )
    private String     nickName;
    @Schema( name = "会员vip(vip等级)" )
    private Integer    vip;
    @Schema( name = "头像ID" )
    private String     headImg;
    @Schema( name = "余额" )
    private BigDecimal accountNow;
    @Schema( name = "充值总金额" )
    private BigDecimal accountCharge;
    @Schema( name = "累计有效打码" )
    private BigDecimal codeNow;
    @Schema( name = "累计需求打码（充值+优惠）" )
    private BigDecimal codeWill;
    @Schema( name = "累计注单" )
    private BigDecimal codeTotal;
    @Schema( name = "升级还需打码量" )
    private BigDecimal nextLevelIntegral = BigDecimal.ZERO;
    @Schema( name = "状态" )
    private Integer    status;
    @Schema( name = "邀请人" )
    private String     inviterCode;
    @Schema( name = "注册类型", description = "0游客 1会员" )
    private Integer    registerType;
}
