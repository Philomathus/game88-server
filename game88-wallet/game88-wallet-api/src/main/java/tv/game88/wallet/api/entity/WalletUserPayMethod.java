package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    private Long methodId;

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
     * 银行账号
     */
    private String bankAccount;

    /**
     * 银行ID
     */
    private Long bankId;

    /**
     * 归属地-省
     */
    private String payAddrProvince;

    /**
     * 归属地-市
     */
    private String payAddrCity;

    /**
     * 收款码图片
     */
    private String payPicAddr;

    /**
     * 审核状态
     */
    // 0 未审核 1已审核 2审核不通过
    private Integer auditStatus;

    /**
     * 绑定时间
     */
    private LocalDateTime createTime;

    @TableField(exist = false)
    private Long    oldBankId;
    @TableField(exist = false)
    private String  oldRealName;
    @TableField(exist = false)
    private String  oldBankAccount;
}