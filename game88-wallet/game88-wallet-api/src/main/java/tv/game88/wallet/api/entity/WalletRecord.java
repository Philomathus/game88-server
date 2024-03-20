package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
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
     * 交易号
     */
    @TableId( type = IdType.INPUT )
    private String tradeNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 钱包用户ID
     */
    private String userId;

    /**
     * 交易类型 （1-下分充值 2-上分提现）
     */
    private Integer tradeType;

    /**
     * 交易数量
     */
    private Long amount;

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
    //0 处理失败，1 处理成功 ，2 处理中
    private Integer status;

    /**
     * 通知状态
     */
    //0 无需通知, 1 通知成功, 2 通知失败
    private Integer notifyStatus;

    /**
     * 通知结果
     */
    private String notifyResult;
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

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String beginTime;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String endTime;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String minAmount;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String maxAmount;

}