package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import tv.game88.wallet.api.type.WalletTransEnum;

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
     * 卖家ID
     */
    private String sellerId;

    /**
     * 买家ID
     */
    private String buyerId;

    /**
     * 请求数量
     */
    private Long amount;

    /**
     * 买家付款方式ID
     */
    private Long buyerPayMethodId;

    /**
     * 卖家收款方式ID
     */
    private Long sellerPayMethodId;

    /**
     * 交易凭证
     */
    private String transCertPic;

    /**
     * 状态
     */
    private WalletTransEnum status;

    /**
     * 买家确认购买时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime buyerConfirmBuyTime;

    /**
     * 卖家确认交易时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime sellerConfirmTransTime;

    /**
     * 买家确认转账时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime buyerConfirmTransferTime;

    /**
     * 卖家未收到转账时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime sellerNotReceivedTime;

    /**
     * 交易取消时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime cancelTime;

    /**
     * 交易完成时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime successTransTime;

    /**
     * 付款时间 秒
     */
    // 买家确认转账时间 - 卖家确认交易时间
    private Integer transferTimeSec;

    /**
     * 放币时间 秒
     */
    // 交易完成时间 - 买家确认转账时间
    private Integer receivedTimeSec;

    /**
     * 交易行为详情
     */
    private String remark;
}