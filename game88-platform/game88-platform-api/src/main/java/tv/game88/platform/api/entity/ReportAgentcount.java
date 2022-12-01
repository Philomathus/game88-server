package tv.game88.platform.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 代理统计，主要用于代理渠道的统计对象 report_agentcount
 *
 * @author 77tv
 * @date 2021-01-26
 */
@Data
public class ReportAgentcount implements Serializable {

    private String repId;

    /**
     * 渠道编码
     */
    @Excel( name = "渠道编码", orderNum = "0" )
    private String agentcode;

    /**
     * 邀请账号
     */
    @Excel( name = "邀请账号", orderNum = "1" )
    private String agentname;

    /**
     * 统计时间
     */
    private String agenttime;

    /**
     * 当日新注册人数
     */
    private Long newmember;

    /**
     * 当日新注册人数
     */
    @Excel( name = "当日/总(注册人数)", orderNum = "2" )
    private String regisNumber;


    /**
     * 总邀请人数
     */
    private Long totalmember;

    /**
     * 公司入款（首充）
     */
    @Excel( name = "公司入款（首充）", orderNum = "3" )
    private String gsRukuanjine;

    /**
     * 线上入款（首充）
     */
    @Excel( name = "线上入款（首充）", orderNum = "4" )
    private String xsRukuanjine;

    /**
     * 手工入款（首充）
     */
    @Excel( name = "手工入款（首充）", orderNum = "5" )
    private String sgRukuanjine;

    /**
     * 入款总（首充）
     */
    @Excel( name = "入款总（首充）", orderNum = "6" )
    private String totalfristRukuanjine;

    /**
     * 出款金额（首次）
     */
    private String chukuanjine;

    /**
     * 总入款（当日总）
     */
    private String totalRukuanjine;
    /**
     * 总入款人数（当日总）
     */
    private String totalRukuanrenshu;
    /**
     * 总入款笔数（当日总）
     */
    private String totalRukuanbishu;

    private String totalRukuanjineAll;

    /**
     * 总出款（当日总）
     */
    @Excel( name = "人/笔/金额（出款日总）", orderNum = "7" )
    private String totalChukuanjine;

    /**
     * 总出款（当日总）
     */
    @Excel( name = "人/笔/金额（入款日总）", orderNum = "8" )
    private String rukuanjine;

    /**
     * 进入直播间次数
     */
    private Long totalEnterlivetimes;

    /**
     * 代理线活跃的安卓用户
     */
    private Long totalActiveandroid;

    /**
     * 代理线活跃的苹果用户
     */
    private Long totalActiveios;

    private String totalEnterliveActive;

    /**
     * 代理线送礼
     */
    @Excel( name = "送礼次数/金额", orderNum = "9" )
    private String totalGiveprop;


    /**
     * 代理线送礼
     */
    @Excel( name = "直播间次数/活跃安卓/活跃苹果", orderNum = "10" )
    private String ios;

    private String code;

    @JsonIgnore
    private Map<String, Object> params = new HashMap<>();

    public String getRegisNumber() {
        return newmember + "/" + totalmember;
    }

    public String getIos() {
        return totalEnterlivetimes + "/" + totalActiveandroid + '/' + totalActiveios;
    }

    public String getRukuanjine() {
        return totalRukuanrenshu + "/" + totalRukuanbishu + '/' + totalRukuanjine;
    }

    public String getTotalRukuanjineAll() {
        if ( totalRukuanjine != null && totalRukuanrenshu != null && totalRukuanbishu != null ) {
            return totalRukuanrenshu + "/" + totalRukuanbishu + "/" + totalRukuanjine;
        }
        return totalRukuanjineAll;
    }

    public String getTotalEnterliveActive() {
        if ( totalEnterlivetimes != null && totalActiveandroid != null && totalActiveios != null ) {
            return totalEnterlivetimes + "/" + totalActiveandroid + "/" + totalActiveios;
        }
        return totalEnterliveActive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "repId", getRepId() )
                .append( "agentcode", getAgentcode() )
                .append( "agentname", getAgentname() )
                .append( "agenttime", getAgenttime() )
                .append( "newmember", getNewmember() )
                .append( "totalmember", getTotalmember() )
                .append( "gsRukuanjine", getGsRukuanjine() )
                .append( "xsRukuanjine", getXsRukuanjine() )
                .append( "sgRukuanjine", getSgRukuanjine() )
                .append( "totalfristRukuanjine", getTotalfristRukuanjine() )
                .append( "chukuanjine", getChukuanjine() )
                .append( "totalRukuanjine", getTotalRukuanjine() )
                .append( "totalChukuanjine", getTotalChukuanjine() )
                .append( "totalEnterlivetimes", getTotalEnterlivetimes() )
                .append( "totalActiveandroid", getTotalActiveandroid() )
                .append( "totalActiveios", getTotalActiveios() )
                .append( "totalGiveprop", getTotalGiveprop() )
                .toString();
    }
}