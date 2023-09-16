package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 钱包用户表
 *
 * @TableName wallet_user
 */
@TableName( value = "wallet_user" )
@Data
public class WalletUser implements Serializable {
    /**
     * 钱包ID
     */
    @TableId( type = IdType.INPUT )
    private String        id;
    /**
     * 昵称
     */
    private String        nickName;
    /**
     * 登录密码
     */
    private String        password;
    /**
     * OTP密钥
     */
    private String        secretKey;
    /**
     * 手机号
     */
    private String        phone;
    /**
     * 金额
     */
    private Long          amount;
    /**
     * 资金密码
     */
    private String        fundPassword;
    /**
     * 是否实名认证 (0 未认证 1 申请认证 2 已认证)
     */
    private Integer       isVerified;
    /**
     * 实名认证时间
     */
    private LocalDateTime verifiedTime;
    /**
     * 真实姓名
     */
    private String        realName;
    /**
     * 身份证号码
     */
    private String        idNumber;
    /**
     * 身份证正面图片地址
     */
    private String        idFrontPic;
    /**
     * 身份证背面图片地址
     */
    private String        idBackPic;
    /**
     * 信用等级 (默认1级 最高5级)
     */
    private Integer       creditRating;
    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createdTime;
    /**
     * 用户状态 (0 禁用 1正常 2 锁定)
     */
    private Integer       status;
    /**
     * 登录IP
     */
    private String        loginIp;
    /**
     * 设备ID
     */
    private String        deviceId;
    /**
     * 累积充值金额
     */
    private Long          totalCharge;
    /**
     * 累积出售金额
     */
    private Long          totalSale;
    /**
     * 买单次数
     */
    private Long          buyOrderNum;
    /**
     * 卖单次数
     */
    private Long          sellOrderNum;
    /**
     * 手机型号
     */
    private String        phoneModel;
    /**
     * 登录设备
     */
    private Integer       loginDevice;
    /**
     * 登录时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime loginTime;
    /**
     * 登录域名
     */
    private String        linkUrl;
}