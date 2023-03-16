package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * memberRechargeOnline对象 member_recharge_online
 *
 * @author mengJun
 */
@Data
@NoArgsConstructor
public class MemberRechargeOnline {
    @TableId(type = IdType.INPUT)
    @Excel(name = "本系统订单号", orderNum = "0")
    private String orderNo;
    @Excel(name = "会员编号", orderNum = "1")
    private String memberId;
    private Long platformId;
    private Long channelId;
    @Excel(name = "上游订单号", orderNum = "4")
    private String upperOrderNo;
    @Excel(name = "请求金额", orderNum = "5")
    private BigDecimal money;
    @Excel(name = "实际到账金额", orderNum = "6")
    private BigDecimal realMoney;
    private String paymentAddress;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "商户下单时间", databaseFormat = "yyyy-MM-dd HH:mm:ss" , orderNum = "7")
    private LocalDateTime payTime;
    private Integer status;
    @Excel(name = "是否是人工补单", orderNum = "9")
    private Boolean patchOrder;
    @Excel(name = "备注", orderNum = "10")
    private String remark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", databaseFormat = "yyyy-MM-dd HH:mm:ss", orderNum = "11")
    private LocalDateTime updateTime;
    @Excel(name = "是否首次", orderNum = "12")
    @TableField(jdbcType = JdbcType.TINYINT)
    private Boolean first;
    @Excel(name = "通道费率", orderNum = "13")
    private BigDecimal rate;

    @Excel(name = "支付通道名称", orderNum = "2")
    @TableField(exist = false)
    private String channelName;
    @TableField(exist = false)
    @Excel(name = "支付平台名称", orderNum = "3")
    private String platformName;
    @TableField(exist = false)
    private BigDecimal currentSuccessRate;
    @TableField(exist = false)
    private BigDecimal currentSuccessRateStr;
    @TableField(exist = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer googleAuthCode;
    @TableField(exist = false)
    private String rateStr;

    @TableField(exist = false)
    @Excel(name = "状态", orderNum = "8")
    private String statusStr;

    public String getCurrentSuccessRateStr() {
        if (currentSuccessRate != null) {
            return currentSuccessRate
                    .multiply(new BigDecimal(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .toString()
                    .concat("%");
        }
        return "";
    }

    public String getRateStr() {
        if (rate != null) {
            String payRateStr = rate.multiply(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP).toString();
            if (payRateStr.endsWith("0")) {
                payRateStr = rate.multiply(new BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toString();
            }
            return payRateStr.concat("%");
        }
        return "";
    }

    public String getStatusStr() {
        if (status == 1)
            return "成功";
        else if (status == 0)
            return "失败";
        else
            return "待确认";

    }
}