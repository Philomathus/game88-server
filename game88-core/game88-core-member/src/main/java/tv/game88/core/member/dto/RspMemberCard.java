package tv.game88.core.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;

@Data
public class RspMemberCard {

    @ApiModelProperty( value = "编号" )
    private Long    id;
    @ApiModelProperty( value = "银行账号" )
    private String  bankAccount;
    @ApiModelProperty( value = "开户地址" )
    private String  bankAddress;
    @ApiModelProperty( value = "银行编码" )
    private String  bankCode;
    @ApiModelProperty( value = "银行名称" )
    private String  bankName;
    @ApiModelProperty( value = "开户用户" )
    private String  realName;
    @ApiModelProperty( value = "是否默认" )
    private boolean dv;
    @ApiModelProperty( value = "银行图标地址" )
    private String  bankIcon;
    @ApiModelProperty( value = "银行真实地址" )
    private String  realBankAddress;
    @ApiModelProperty( value = "结束颜色" )
    private String  colorEnd   = "#ffffff";
    @ApiModelProperty( value = "开始颜色" )
    private String  colorStart = "#ffffff";

    public String getBankIcon() {
        if ( StringUtils.hasText( bankIcon ) && !bankIcon.startsWith( "http" ) ) {
            return ConfigDomainCacheUtil.me.getValue( "domain.oss" ) + bankIcon;
        }
        return bankIcon;
    }
}
