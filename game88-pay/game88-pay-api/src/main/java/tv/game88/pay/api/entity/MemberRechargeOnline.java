package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * memberRechargeOnline对象 member_recharge_online
 *
 * @author 77lm
 * @date 2021-10-06
 */
@Data
public class MemberRechargeOnline {
    private static final long serialVersionUID = 1L;

    /** 系统编号 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会员编号 */
    @Excel(name = "会员编号")
    private String memberId;

    /** 支付平台编号 */
    @Excel(name = "支付平台编号")
    private Integer platformId;

    /** 支付通道编码 */
    @Excel(name = "支付通道编码")
    private Long channelId;

    /** 本系统订单号 */
    @Excel(name = "本系统订单号")
    private String orderNo;

    /** 上游订单号 */
    @Excel(name = "上游订单号")
    private String tradeSn;

    /** 请求金额 */
    @Excel(name = "币种请求金额")
    private BigDecimal money;

    /** 实际到账金额 */
    @Excel(name = "币种实际到账金额")
    private BigDecimal subMoney;

    /** 币种编码 */
    @Excel(name = "币种编码")
    private String currencyCode;

    /** 平台金额 */
    @Excel(name = "平台金额")
    private BigDecimal platformMoney;

    /** 支付接口的支付地址 */
    @Excel(name = "支付接口的支付地址")
    private String paymentCode;

    /** 商户下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "商户下单时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /** 状态(1 成功0失败 -1待确认) */
    @Excel(name = "状态(1 成功0失败 -1待确认)")
    private String status;

    /** 是否是人工补单 */
    @Excel(name = "是否是人工补单")
    private Integer isPatchOrder;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 是否首次1是0否 */
    @Excel(name = "是否首次1是0否")
    private Long first;

    /** 通道费率 */
    @Excel(name = "通道费率")
    private BigDecimal payRate;

    /** 承担费率主体 */
    @Excel(name = "承担费率主体")
    private String isMemberBear;

    @TableField(exist = false)
    private String channelName;

    @TableField(exist = false)
    private String platformName;

    @TableField(exist = false)
    private BigDecimal currentSuccessRate;

    @TableField(exist = false)
    private String googleAuthCode;

    public String getCurrentSuccessRateStr() {
        if ( currentSuccessRate != null ) {
            return currentSuccessRate.multiply( new BigDecimal( 100 ) ).setScale( 0, RoundingMode.HALF_UP ).toString().concat(
                    "%" );
        }
        return "";
    }

    public String getPayRateStr() {
        if ( payRate != null ) {
            String payRateStr = payRate.multiply( new BigDecimal( 100 ) ).setScale( 1, RoundingMode.HALF_UP ).toString();
            if ( payRateStr.endsWith( "0" ) ) {
                payRateStr = payRate.multiply( new BigDecimal( 100 ) ).setScale( 0, RoundingMode.HALF_UP ).toString();
            }
            return payRateStr.concat( "%" );
        }
        return "";
    }

    public String getStatusDes() {
        if ( StringUtils.hasText( status ) ) {
            switch ( status ) {
                case "1":
                    return "成功";
                case "0":
                    return "失败";
                case "-1":
                    return "待确认";
            }
        }
        return "待确认";
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("memberId", getMemberId())
            .append("platformId", getPlatformId())
            .append("channelId", getChannelId())
            .append("orderNo", getOrderNo())
            .append("tradeSn", getTradeSn())
            .append("money", getMoney())
            .append("subMoney", getSubMoney())
            .append("paymentCode", getPaymentCode())
            .append("payTime", getPayTime())
            .append("status", getStatus())
            .append("isPatchOrder", getIsPatchOrder())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("first", getFirst())
            .toString();
    }
}