package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.util.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;

@Data
public class RspMemberCard {

    @Schema( name = "编号" )
    private Long    id;
    @Schema( name = "银行账号" )
    private String  bankAccount;
    @Schema( name = "开户地址" )
    private String  bankAddress;
    @Schema( name = "银行编码" )
    private String  bankCode;
    @Schema( name = "银行名称" )
    private String  bankName;
    @Schema( name = "开户用户" )
    private String  realName;
    @Schema( name = "是否默认" )
    private boolean dv;
    @Schema( name = "银行图标地址" )
    private String  bankIcon;
    @Schema( name = "银行真实地址" )
    private String  realBankAddress;
    @Schema( name = "结束颜色" )
    private String  colorEnd   = "#ffffff";
    @Schema( name = "开始颜色" )
    private String  colorStart = "#ffffff";

    public String getBankIcon() {
        if ( StringUtils.hasText( bankIcon ) && !bankIcon.startsWith( "http" ) ) {
            return ConfigDomainCacheUtil.me.getValue( "domain.oss" ) + bankIcon;
        }
        return bankIcon;
    }
}
