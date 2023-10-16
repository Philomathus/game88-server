package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.wallet.api.constants.ConstantsWallet;

@Data
public class RspTransCenterDetail {
    @Schema( title = "挂单编号" )
    private String  transactionId;
    @Schema( title = "卖家头像" )
    private String  headImg;
    @Schema( title = "是否可拆分" )
    private Boolean canSplit;
    @Schema( title = "售卖数量" )
    private Long    amount;
    @Schema( title = "30日成单数" )
    private Integer successNumMonth;
    @Schema( title = "30日成单率" )
    private String  successRateMonth;
    @Schema( title = "收款方式类型", description = "英文逗号,分割" )
    private String  payMethodTypes;

    @Schema( title = "status", description = "status" )
    private Integer status;

    public String getHeadImg() {
        if ( StringUtils.isBlank( headImg ) ) {
            return ConstantsWallet.DEFAULT_HEAD_IMAGE_URL;
        }
        return ConfigDomainCacheUtil.me.getDomainOssValue() + headImg;
    }
}
