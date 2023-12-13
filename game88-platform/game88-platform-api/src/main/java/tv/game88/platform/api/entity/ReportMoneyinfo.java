package tv.game88.platform.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额对象 report_moneyinfo
 *
 * @author 77tv
 * @date 2021-01-25
 */
@Data
public class ReportMoneyinfo implements Serializable {

    private String repId;

    /**
     * 报表时间
     */
    @Excel( name = "报表时间" )
    private String reptime;

    /**
     * 公司入款人数
     */
    @Excel( name = "公司入款人数" )
    private Long gsRukuanrenshu;

    /**
     * 公司入款金额
     */
    @Excel( name = "公司入款金额" )
    private BigDecimal gsRukuanjine;

    /**
     * 线上入款人数
     */
    @Excel( name = "线上入款人数" )
    private Long xsRukunanrenshu;

    /**
     * 线上入款金额
     */
    @Excel( name = "线上入款金额" )
    private BigDecimal xsRukunanjine;

    /**
     * 人工入款人数
     */
    @Excel( name = "人工入款人数" )
    private Long rgRukunanrenshu;

    /**
     * 人工入款金额
     */
    @Excel( name = "人工入款金额" )
    private BigDecimal rgRukunanjine;

    /**
     * 平台优惠人数
     */
    @Excel( name = "平台优惠人数" )
    private Long palmYouhuirenshu;

    /**
     * 平台优惠金额
     */
    @Excel( name = "平台优惠金额" )
    private BigDecimal palmYouhuijine;

    /**
     * 入款总人数
     */
    @Excel( name = "入款总人数" )
    private Long totalRukuanrenshu;

    /**
     * 入款总金额
     */
    @Excel( name = "入款总金额" )
    private BigDecimal totalRukuanjine;

    /**
     * 公司出款总人数
     */
    @Excel( name = "公司出款总人数" )
    private Long totalChukuanrenshu;

    /**
     * 公司出款总金额
     */
    @Excel( name = "公司出款总金额" )
    private BigDecimal totalChukuanjine;

    /**
     * 预估会员剩余积分
     */
    @Excel( name = "预估会员剩余积分" )
    private BigDecimal totalAccount;

    /**
     * 每日平台盈利
     */
    @Excel( name = "每日平台盈利" )
    private BigDecimal totalProfile;

    /**
     * 每日送礼金额
     */
    @Excel( name = "每日送礼金额" )
    private BigDecimal totalGiveprop;

    /**
     * 合计剩余偏差
     */
    @Excel( name = "合计剩余偏差" )
    private BigDecimal totalLast;

    private BigDecimal totalActiveprop;

    @Excel( name = "usdt人数" )
    private Long       usdtRenshu;
    @Excel( name = "usdt金额" )
    private BigDecimal usdtJine;

    private BigDecimal paymentAmount;//入款总额
    private BigDecimal outMoney;//出款总额
    private BigDecimal countMoney;//合计
    private BigDecimal totalAccountGifts;//送礼总金额

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private Date updateTime;

    @JsonIgnore
    private Map<String, Object> params = new HashMap<>();

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "repId", getRepId() )
                .append( "reptime", getReptime() )
                .append( "gsRukuanrenshu", getGsRukuanrenshu() )
                .append( "gsRukuanjine", getGsRukuanjine() )
                .append( "xsRukunanrenshu", getXsRukunanrenshu() )
                .append( "xsRukunanjine", getXsRukunanjine() )
                .append( "rgRukunanrenshu", getRgRukunanrenshu() )
                .append( "rgRukunanjine", getRgRukunanjine() )
                .append( "palmYouhuirenshu", getPalmYouhuirenshu() )
                .append( "palmYouhuijine", getPalmYouhuijine() )
                .append( "totalRukuanrenshu", getTotalRukuanrenshu() )
                .append( "totalRukuanjine", getTotalRukuanjine() )
                .append( "totalChukuanrenshu", getTotalChukuanrenshu() )
                .append( "totalChukuanjine", getTotalChukuanjine() )
                .append( "totalAccount", getTotalAccount() )
                .append( "totalProfile", getTotalProfile() )
                .append( "totalGiveprop", getTotalGiveprop() )
                .append( "totalLast", getTotalLast() )
                .toString();
    }
}