package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 钱包交易明细表
 *
 * @TableName wallet_transaction_detail
 */
@TableName( value = "wallet_transaction_detail" )
@Data
public class WalletTransactionDetail implements Serializable {
    /**
     * 交易明细ID
     */
    @TableId( type = IdType.INPUT )
    private String transDetailId;

    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 钱包用户ID
     */
    private String userId;

    /**
     * 请求金额
     */
    private Long amount;

    /**
     * 交易凭证
     */
    private String transCertPic;

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