package tv.game88.wallet.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import tv.game88.common.utils.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private String        platformId;
    /**
     * 商户ID
     */
    private Long          merchantId;
    /**
     * 昵称
     */
    private String        nickName;
    /**
     * 用户头像
     */
    private String        headImg;
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
     * 冻结金额
     */
    private Long          frozenAmount;
    /**
     * 资金密码
     */
    private String        fundPassword;
    /**
     * 是否实名认证 0 未认证 1 申请认证 2 已认证  3 拒绝
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
     * 用户等级
     */
    private Integer       level;
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
     * 累积购买金额
     */
    private Long          totalBuy;
    /**
     * 卖单中金额
     */
    private Long          sellingAmount;
    /**
     * 交易中金额
     */
    private Long          tradingAmount;
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
    /**
     * Remarks
     */
    private String        remarks;

    @TableField( exist = false )
    private String bankAccount;

    @Schema( title = "30日成单数" )
    private Integer successNumMonth;

    @Schema( title = "30日成单率" )
    private String successRateMonth;

    @Schema( title = "30日平均付款时间" )
    private String receivedTimeMonth;

    @Schema( title = "30日平均放币时间" )
    private String transferTimeMonth;

    @TableField( exist = false )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String searchValue;

    @TableField( exist = false )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private Set<String> searchValues;

    public Set<String> getSearchValues() {
        if ( StringUtils.isNotBlank( searchValue ) ) {
            String[]    strings        = searchValue.split( "," );
            Set<String> searchValueSet = new HashSet<>();
            for ( String s : strings ) {
                if ( StringUtils.isNotBlank( s ) ) {
                    searchValueSet.add( s.trim() );
                }
            }
            return searchValueSet;
        }
        return searchValues;
    }

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String beginTime;

    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    @TableField( exist = false )
    private String endTime;
}