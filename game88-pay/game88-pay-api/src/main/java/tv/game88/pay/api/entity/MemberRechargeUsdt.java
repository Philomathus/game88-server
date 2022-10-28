package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * USDT充值对象 pay_usdt_recharge
 *
 * @author 77tv
 * @date 2021-09-14
 */
@Data
public class MemberRechargeUsdt{
    private static final long serialVersionUID = 1L;

    /** 系统编号 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会员编号 */
    @Excel(name = "会员编号")
    private String memberId;

    /** 渠道名称 */
    @Excel(name = "渠道名称")
    private String channelName;

    /** 充值U数量 */
    @Excel(name = "充值U数量")
    private Long rechargeNumber;

    /** 充值U数量 */
    @Excel(name = "充值汇率")
    private BigDecimal rechargeRate;

    /** 充值金额 */
    @Excel(name = "充值金额")
    private BigDecimal rechargeMoney;

    /** 状态(0已提交1通过2驳回) */
    @Excel(name = "状态(1已提交 2拒绝 3通过 0锁定)")
    private String status;

    /** 优惠比例 */
    @Excel(name = "优惠比例")
    private BigDecimal discountBill;

    /** 链名称 */
    @Excel(name = "链名称")
    private String chainName;

    /** 充值地址 */
    @Excel(name = "充值地址")
    private String rechargeAddress;

    /** 交易id */
    @Excel(name = "交易id")
    private String transactionId;

    /** 操作人 */
    @Excel(name = "操作人")
    private String opName;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private Integer googleAuthCode;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("memberId", getMemberId())
            .append("channelName", getChannelName())
            .append("rechargeNumber", getRechargeNumber())
            .append("rechargeRate", getRechargeRate())
            .append("rechargeMoney", getRechargeMoney())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("discountBill", getDiscountBill())
            .append("chainName", getChainName())
            .append("rechargeAddress", getRechargeAddress())
            .append("transactionId", getTransactionId())
            .append("opName", getOpName())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
