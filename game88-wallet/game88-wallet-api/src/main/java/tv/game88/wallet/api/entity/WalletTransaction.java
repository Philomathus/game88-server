package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import tv.game88.wallet.api.type.WalletPayMethodEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易表
 *
 * @TableName wallet_transaction
 */
@TableName( value = "wallet_transaction" )
@Data
public class WalletTransaction implements Serializable {
    /**
     * 交易ID
     */
    @TableId( type = IdType.INPUT )
    private String transactionId;

    /**
     * 钱包用户ID
     */
    private String userId;

    /**
     * 支付类型（冗余）
     */
    private WalletPayMethodEnum payMethodType;

    /**
     * 支付方式ID
     */
    private Integer payMethodId;

    /**
     * 是否可拆分（0 不可拆分 1 可拆分）
     */
    private Boolean canSplit;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 请求时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;

    /**
     * 交易完成时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime successTransTime;
}