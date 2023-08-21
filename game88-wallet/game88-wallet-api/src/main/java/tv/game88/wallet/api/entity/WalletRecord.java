package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包用户上下分记录
 *
 * @TableName wallet_record
 */
@TableName( value = "wallet_record" )
@Data
public class WalletRecord implements Serializable {
    /**
     * 交易ID
     */
    @TableId( type = IdType.INPUT )
    private String tradeId;

    /**
     * 商户ID
     */
    private String merchantId;

    /**
     * 钱包用户ID
     */
    private String userId;

    /**
     * 交易类型 （1-下分充值 2-上分提现）
     */
    private Integer tradeType;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 商户订单号
     */
    private String orderId;

    /**
     * 商户回调地址
     */
    private String notifyUrl;

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
}