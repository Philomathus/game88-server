package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户资金日志表
 *
 * @TableName wallet_user_fund_log
 */
@TableName( value = "wallet_user_fund_log" )
@Data
public class WalletUserFundLog {
    /**
     * 系统编号
     */
    @TableId( type = IdType.INPUT )
    private String id;

    /**
     * 会员ID
     */
    private String userId;

    /**
     * 变化类型
     */
    private Integer type;

    /**
     * 描述
     */
    private String des;

    /**
     * 收入
     */
    private BigDecimal income;

    /**
     * 支出
     */
    private BigDecimal pay;

    /**
     * 变化前余额
     */
    private BigDecimal totalBefore;

    /**
     * 变化后余额
     */
    private BigDecimal total;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;

    /**
     * 备注信息
     */
    private String mark;

    /**
     * 备注订单号
     */
    private String markorder;
}