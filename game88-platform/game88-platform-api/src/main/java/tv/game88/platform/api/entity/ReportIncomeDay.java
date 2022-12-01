package tv.game88.platform.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 【请填写功能名称】对象 report_income_day
 *
 * @author 77tv
 * @date 2021-01-26
 */
@Data
public class ReportIncomeDay implements Serializable {

    /**
     * id
     */
    @Excel( name = "id" )
    private String reppayId;

    /**
     * 收款金额
     */
    @Excel( name = "收款金额" )
    private BigDecimal money;

    /**
     * 线下或者线上
     */
    @Excel( name = "线下或者线上" )
    private String type;

    /**
     * 收款平台
     */
    @Excel( name = "收款平台" )
    private String payplam;

    /**
     * 收款通道
     */
    @Excel( name = "收款通道" )
    private String paychancl;

    /**
     * 收款商户
     */
    @Excel( name = "收款商户" )
    private String paycard;

    /**
     * 时间
     */
    @Excel( name = "时间" )
    private String paydate;

    private BigDecimal countSuccessMoney;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "reppayId", getReppayId() )
                .append( "money", getMoney() )
                .append( "type", getType() )
                .append( "payplam", getPayplam() )
                .append( "paychancl", getPaychancl() )
                .append( "paycard", getPaycard() )
                .append( "paydate", getPaydate() )
                .toString();
    }
}