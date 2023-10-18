package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import tv.game88.wallet.api.type.WalletPayMethodEnum;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

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
     * 支付方式ID列表
     */
    private String payMethodIds;

    /**
     * 支付方式类型列表
     */
    private String payMethodTypes;

    /**
     * 是否可拆分（0 不可拆分 1 可拆分）
     */
    private Boolean canSplit;

    /**
     * 出售数量
     */
    private Long amount;

    /**
     * 最低可购买金额
     */
    private Long minBuyNum;

    /**
     * 状态
     */
    // 0挂单中 1交易中 2交易成功 3取消交易
    private Integer status;

    /**
     * 请求时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;

    /**
     * 交易结束时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime transEndTime;

    /**
     * 30日成单数
     */
    private Integer successNumMonth;

    /**
     * 30日成单率
     */
    private String successRateMonth;

    /**
     * 30日平均付款时间
     */
    private String receivedTimeMonth;

    /**
     * 30日平均放币时间
     */
    private String transferTimeMonth;

    @TableField( exist = false )
    private WalletPayMethodEnum payMethodType;
    @TableField( exist = false )
    private Long                minAmount;
    @TableField( exist = false )
    private Long                maxAmount;
    @TableField( exist = false )
    private List<Integer>       statusList;
    @TableField( exist = false )
    private String       unUserId;
}