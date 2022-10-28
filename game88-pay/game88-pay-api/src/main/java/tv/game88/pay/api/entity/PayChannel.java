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
import java.math.RoundingMode;
import java.util.Date;

/**
 * pay_channel
 *
 * @author 77tv
 * @date 2021-01-27
 */
@Data
public class PayChannel{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 通道名称 */
    @Excel(name = "通道名称")
    private String name;

    /** 支付方式编码 */
    @Excel(name = "支付方式编码")
    private String payMethod;

    /** 充值最低 */
    @Excel(name = "充值最低")
    private BigDecimal rechargeMin;

    /** 充值最高 */
    @Excel(name = "充值最高")
    private BigDecimal rechargeMax;

    /** 状态(1启用0停用) */
    @Excel(name = "状态(1启用0停用)")
    private String status;

    /** 是否允许回调(默认1 允许 0不允许) */
    @Excel(name = "是否允许回调(默认1 允许 0不允许)")
    private String isCanCallback;

    /** 开放层级 0非vip 1vip */
    @Excel(name = "开放层级 0所有 1vip")
    private String openLevelType;

    /** 支付平台编号 */
    @Excel(name = "支付平台编号")
    private Integer payPlatformId;

    /** 支付类型编号 */
    @Excel(name = "支付类型编号")
    private Integer payTypeId;

    /** 优惠比例 */
    @Excel(name = "优惠比例")
    private String discountBill;

    /** 币种编码 */
    @Excel(name = "币种编码")
    private String currencyCode;

    /** 快捷金额 */
    @Excel(name = "快捷金额")
    private String quickAmount;

    /** 通道费率 */
    @Excel(name = "通道费率")
    private BigDecimal payRate;

    /** 通道费率 */
    @Excel(name = "承担费率主体")
    private String isMemberBear;

    /** 创建人 */
    @Excel(name = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date createTime;

    /** 修改人 */
    @Excel(name = "修改人")
    private String updator;

    /**
     * 更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date updateTime;

    /**
     * 银行编码
     */
    private String bankCode;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private String successRate;

    @TableField(exist = false)
    private String payPlatformName;

    @TableField(exist = false)
    private String payTypeName;

    public String getPayRateStr() {
        if (payRate != null) {
            return payRate.multiply(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP).toString().concat("%");
        }
        return "";
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("payMethod", getPayMethod())
            .append("rechargeMin", getRechargeMin())
            .append("rechargeMax", getRechargeMax())
            .append("status", getStatus())
            .append("isCanCallback", getIsCanCallback())
            .append("openLevelType", getOpenLevelType())
            .append("payPlatformId", getPayPlatformId())
            .append("payTypeId", getPayTypeId())
            .append("discountBill", getDiscountBill())
            .append("quickAmount", getQuickAmount())
            .append("remark", getRemark())
            .append("creator", getCreator())
            .append("createTime", getCreateTime())
            .append("updator", getUpdator())
            .append("updateTime", getUpdateTime())
            .append("payRate", getPayRate())
            .toString();
    }
}
