package tv.game88.core.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RspMember {
    @ApiModelProperty( value = "登录令牌" )
    private String     token;
    @ApiModelProperty( value = "会员ID" )
    private String     id;
    @ApiModelProperty( value = "昵称" )
    private String     nickName;
    @ApiModelProperty( value = "会员vip(vip等级)" )
    private Integer    vip;
    @ApiModelProperty( value = "头像ID" )
    private String     headImg;
    @ApiModelProperty( value = "余额" )
    private BigDecimal accountNow;
    @ApiModelProperty( value = "充值总金额" )
    private BigDecimal accountCharge;
    @ApiModelProperty( value = "累计有效打码" )
    private BigDecimal codeNow;
    @ApiModelProperty( value = "累计需求打码（充值+优惠）" )
    private BigDecimal codeWill;
    @ApiModelProperty( value = "累计注单" )
    private BigDecimal codeTotal;
    @ApiModelProperty( value = "升级还需打码量" )
    private BigDecimal nextLevelIntegral = BigDecimal.ZERO;
    @ApiModelProperty( value = "状态" )
    private Integer    status;
    @ApiModelProperty( value = "邀请人" )
    private String     inviterCode;
    @ApiModelProperty( value = "0游客 1会员" )
    private Integer    registerType;
}
