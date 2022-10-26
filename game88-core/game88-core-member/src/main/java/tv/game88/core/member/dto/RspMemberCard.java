package tv.game88.core.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.util.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;

@Data
public class RspMemberCard {

    @Schema( title = "编号" )
    private Long    id;
    @Schema( title = "银行账号" )
    private String  bankAccount;
    @Schema( title = "开户地址" )
    private String  bankAddress;
    @Schema( title = "银行编码" )
    private String  bankCode;
    @Schema( title = "银行名称" )
    private String  bankName;
    @Schema( title = "开户用户" )
    private String  realName;
    @Schema( title = "是否默认" )
    private boolean dv;
    @Schema( title = "银行图标地址" )
    private String  bankIcon;
    @Schema( title = "银行真实地址" )
    private String  realBankAddress;
    @Schema( title = "结束颜色" )
    private String  colorEnd   = "#ffffff";
    @Schema( title = "开始颜色" )
    private String  colorStart = "#ffffff";

    public String getBankIcon() {
        if ( StringUtils.hasText( bankIcon ) && !bankIcon.startsWith( "http" ) ) {
            return ConfigDomainCacheUtil.me.getValue( "domain.oss" ) + bankIcon;
        }
        return bankIcon;
    }
}
