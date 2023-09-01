package tv.game88.general.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间对象 report_plam_com
 *
 * @author 77tv
 * @date 2021-01-25
 */
@Data
public class ReportPlamCom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String repId;


    private String classOne;


    private String classOnename;

    private String agentPlatform;

    private String classTwo;


    @Excel( name = "名称", orderNum = "0" )
    private String classTwoname;

    @Excel( name = "类型", orderNum = "1" )
    private String type;

    /** 对应子类报表的统计值 */
    @Excel( name = "金额", orderNum = "2" )
    private BigDecimal tValue;

    @Excel( name = "报表时间", orderNum = "3" )
    private String reporttime;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private Date updateTime;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "repId", getRepId() )
                .append( "classOne", getClassOne() )
                .append( "classOnename", getClassOnename() )
                .append( "classTwo", getClassTwo() )
                .append( "classTwoname", getClassTwoname() )
                .append( "type", getType() )
                .append( "tValue", getTValue() )
                .append( "reporttime", getReporttime() )
                .toString();
    }
}
