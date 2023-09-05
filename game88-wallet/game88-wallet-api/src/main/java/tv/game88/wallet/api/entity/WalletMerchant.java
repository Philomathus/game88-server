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
 * 钱包商户表
 *
 * @TableName wallet_merchant
 */
@TableName( value = "wallet_merchant" )
@Data
public class WalletMerchant implements Serializable {
    /**
     * 商户ID
     */
    @TableId( type = IdType.AUTO )
    private Long id;

    /**
     * 商户名称
     */
    private String name;

    /**
     * 账号 (登录用)
     */
    private String account;

    /**
     * 登录密码
     */
    private String password;

    /**
     * OTP密钥
     */
    private String secretKey;

    /**
     * MD5密钥
     */
    private String md5Key;

    /**
     * 状态 (0 禁用 1正常)
     */
    private Integer status;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 余额
     */
    private BigDecimal amount;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 登录时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime loginTime;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updatedTime;

    /**
     * 备注
     */
    private String remark;
}