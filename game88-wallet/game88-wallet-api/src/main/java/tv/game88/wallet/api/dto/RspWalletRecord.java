package tv.game88.wallet.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RspWalletRecord {
    /**
     * 交易号
     */
    private String tradeNo;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 商户订单号
     */
    private String orderNo;

    /**
     * 商户回调地址
     */
    private String notifyUrl;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 商户备注
     */
    private String remark;

    /**
     * 交易时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;

    /**
     * 处理时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;

    private String sign;
}
