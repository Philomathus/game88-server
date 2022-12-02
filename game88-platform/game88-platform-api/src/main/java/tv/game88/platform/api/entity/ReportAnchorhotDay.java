package tv.game88.platform.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 贡献榜对象 report_anchorhot_day
 *
 * @author 77tv
 * @date 2021-01-28
 */
@Data
public class ReportAnchorhotDay {

    /**
     * id
     */
    @Excel( name = "id" )
    private String repId;

    /**
     * 主播ID
     */
    @Excel( name = "主播ID" )
    private Integer anchorid;

    /**
     * 主播昵称
     */
    @Excel( name = "主播昵称" )
    private String nickname;

    /**
     * 每日热度
     */
    @Excel( name = "每日热度" )
    private BigDecimal dayTicket;

    /**
     * 热度排名
     */
    @Excel( name = "热度排名" )
    private Integer sort;

    /**
     * 距离上一次差值
     */
    @Excel( name = "距离上一次差值" )
    private BigDecimal dayTicketUpdiff;

    /**
     * 日榜
     */
    @Excel( name = "日榜" )
    private String repdate;

    /**
     * 第几天
     */
    @Excel( name = "第几天" )
    private String num;

    /**
     * 更新日期
     */
    @DateTimeFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新日期", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date reptime;

    /**
     * 主播头像
     */
    @Excel( name = "主播头像" )
    private String headImage;

    private Integer type;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "repId", getRepId() )
                .append( "anchorid", getAnchorid() )
                .append( "nickname", getNickname() )
                .append( "dayTicket", getDayTicket() )
                .append( "sort", getSort() )
                .append( "dayTicketUpdiff", getDayTicketUpdiff() )
                .append( "repdate", getRepdate() )
                .append( "num", getNum() )
                .append( "reptime", getReptime() )
                .append( "headImage", getHeadImage() )
                .toString();
    }

    public Integer getType() {
        return type;
    }

    public void setType( Integer type ) {
        this.type = type;
    }
}
