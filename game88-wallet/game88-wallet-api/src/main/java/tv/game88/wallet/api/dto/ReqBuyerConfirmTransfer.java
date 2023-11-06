package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;

import jakarta.validation.constraints.NotBlank;

@EqualsAndHashCode( callSuper = true )
@Data
public class ReqBuyerConfirmTransfer extends ReqTransactionDetail {
    @Schema( title = "交易凭证" )
    @NotBlank( message = "请上传交易凭证" )
    private String transCertPic;

    public String getTransCertPic() {
        if ( StringUtils.isNotBlank( transCertPic ) ) {
            return transCertPic.replaceAll( ConfigDomainCacheUtil.me.getDomainOssValue(), "" );
        }
        return transCertPic;
    }
}
