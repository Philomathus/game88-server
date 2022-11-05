package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * memberRechargeOnline对象 member_recharge_online
 *
 * @author 77lm
 * @date 2021-10-06
 */
@Data
@NoArgsConstructor
public class MemberRechargeOnline {
    @TableId( type = IdType.INPUT )
    @Excel( name = "本系统订单号" )
    private String        orderNo;
    @Excel( name = "会员编号" )
    private String        memberId;
    @Excel( name = "支付平台编号" )
    private Long          platformId;
    @Excel( name = "支付通道编号" )
    private Long          channelId;
    @Excel( name = "上游订单号" )
    private String        upperOrderNo;
    @Excel( name = "请求金额" )
    private BigDecimal    money;
    @Excel( name = "实际到账金额" )
    private BigDecimal    realMoney;
    @Excel( name = "支付接口的支付地址" )
    private String        paymentAddress;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "商户下单时间", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime payTime;
    @Excel( name = "状态(-1待确认0失败1成功)" )
    private Integer       status;
    @Excel( name = "是否是人工补单" )
    private Boolean       patchOrder;
    @Excel( name = "备注" )
    private String        remark;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;
    @Excel( name = "是否首次" )
    private Boolean       first;
    @Excel( name = "通道费率" )
    private BigDecimal    rate;

    @TableField( exist = false )
    private String     channelName;
    @TableField( exist = false )
    private String     platformName;
    @TableField( exist = false )
    private BigDecimal currentSuccessRate;
    @TableField( exist = false )
    private BigDecimal currentSuccessRateStr;
    @TableField( exist = false )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private Integer    googleAuthCode;
    @TableField( exist = false )
    private String     rateStr;

    public String getCurrentSuccessRateStr() {
        if ( currentSuccessRate != null ) {
            return currentSuccessRate.multiply( new BigDecimal( 100 ) ).setScale( 0, RoundingMode.HALF_UP ).toString()
                    .concat( "%" );
        }
        return "";
    }

    public String getRateStr() {
        if ( rate != null ) {
            String payRateStr = rate.multiply( new BigDecimal( 100 ) ).setScale( 1, RoundingMode.HALF_UP ).toString();
            if ( payRateStr.endsWith( "0" ) ) {
                payRateStr = rate.multiply( new BigDecimal( 100 ) ).setScale( 0, RoundingMode.HALF_UP ).toString();
            }
            return payRateStr.concat( "%" );
        }
        return "";
    }
}