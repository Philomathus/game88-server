package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import tv.game88.wallet.api.type.WalletPayMethodEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName wallet_user_pay_method
 */
@TableName( value = "wallet_user_pay_method" )
@Data
public class WalletUserPayMethod implements Serializable {
    @TableId( type = IdType.AUTO )
    private Integer methodId;

    /**
     * 支付类型
     */
    private WalletPayMethodEnum methodType;

    /**
     * 钱包用户ID
     */
    private String userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 支付方式账号
     */
    private String account;

    /**
     * 银行ID
     */
    private Integer bankId;

    /**
     * 银行卡归属地
     */
    private String bankAddress;

    /**
     * 绑定时间
     */
    private LocalDateTime createTime;
}