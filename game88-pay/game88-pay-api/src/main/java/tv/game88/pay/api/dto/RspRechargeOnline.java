package tv.game88.pay.api.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class RspRechargeOnline {
    /**
     * 支付平台编号
     */
    private String platformId;

    /**
     * 支付通道编码
     */
    private String channelId;

    @Excel( name = "请求金额", orderNum = "1" )
    private BigDecimal money;

    @Excel( name = "实际金额", orderNum = "2" )
    private BigDecimal realMoney;

    @Excel( name = "支付平台名称", orderNum = "3" )
    private String platformName;

    @Excel( name = "支付通道名称", orderNum = "4" )
    private String channelName;

    @Excel( name = "回调时间", databaseFormat = "yyyy-MM-dd HH:mm:ss", exportFormat = "yyyy-MM-dd HH:mm:ss" )
    private String updateTime;

    @Excel( name = "通道费率", orderNum = "5" )
    private BigDecimal channelPayRate;

    @Excel( name = "手续费", orderNum = "5" )
    private BigDecimal handlingfee;

    @Excel( name = "结算金额", orderNum = "7" )
    private BigDecimal remaining;

    public BigDecimal getHandlingfee() {
        if ( channelPayRate == null ) {
            return BigDecimal.ZERO;
        }
        if ( channelPayRate.compareTo( BigDecimal.ZERO ) > 0 ) {
            if ( realMoney == null ) {
                realMoney = money;
            }
            return realMoney.multiply( channelPayRate ).setScale( 2, RoundingMode.HALF_UP );
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getRemaining() {
        if ( channelPayRate == null ) {
            return realMoney;
        }
        if ( channelPayRate.compareTo( BigDecimal.ZERO ) > 0 ) {
            if ( realMoney == null ) {
                realMoney = money;
            }
            return realMoney.subtract( realMoney.multiply( channelPayRate ) ).setScale( 2, RoundingMode.HALF_UP );
        }
        return realMoney;
    }
}
