package tv.game88.wallet.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.wallet.api.type.WalletPayMethodEnum;
import tv.game88.wallet.api.type.WalletTransEnum;

import java.time.LocalDateTime;

@Data
public class RspBuyOrderDetail {
    @Schema( title = "买单编号" )
    private String              transDetailId;
    @Schema( title = "购买数量" )
    private Long                amount;
    @Schema( title = "交易状态" )
    private WalletTransEnum     status;
    @Schema( title = "收款方式类型" )
    private WalletPayMethodEnum payMethodType;
    @Schema( title = "交易凭证" )
    private String              transCertPic;
    @Schema( title = "卖家ID" )
    private String              sellerId;
    @Schema( title = "买家ID" )
    private String              buyerId;

    @Schema( title = "订单倒计时 - 秒" )
    private Long countdownSec;

    @Schema( title = "卖方收款信息" )
    private RspPayMethod2 sellerPayMethod;
    @Schema( title = "买方付款信息" )
    private RspPayMethod2 buyerPayMethod;

    @Schema( title = "买方信用信息" )
    private RspCreditInfo creditInfo;

    @Schema( title = "事务开始时间" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime transStartTime;

    public String getTransCertPic() {
        if ( StringUtils.isNotBlank( transCertPic ) ) {
            return ConfigDomainCacheUtil.me.getDomainOssValue() + transCertPic;
        }
        return transCertPic;
    }
}
