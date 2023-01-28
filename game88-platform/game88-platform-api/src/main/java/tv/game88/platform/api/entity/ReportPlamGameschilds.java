package tv.game88.platform.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 游戏投注报表子表对象 report_plam_gameschilds
 *
 * @author 77tv
 * @date 2021-02-20
 */
@Data
public class ReportPlamGameschilds {

    /**
     * id
     */
    private String gameUuid;

    /**
     * 平台编号
     */
    @Excel( name = "平台编号" )
    private String gameagent;

    /**
     * 平台名称
     */
    @Excel( name = "平台名称" )
    private String gameplame;

    /**
     * 子平台编号
     */
    @Excel( name = "子平台编号" )
    private String agentchild;

    /**
     * 子平台名称
     */
    @Excel( name = "子平台名称" )
    private String agentchildname;

    /**
     * 投注人数
     */
    @Excel( name = "投注人数" )
    private BigDecimal gamepepole;

    /**
     * 投注比数
     */
    @Excel( name = "投注比数" )
    private BigDecimal gametouzhu;

    /**
     * 总投注金额
     */
    @Excel( name = "总投注金额" )
    private BigDecimal gamecell;

    /**
     * 有效投注金额
     */
    @Excel( name = "有效投注金额" )
    private BigDecimal gamebet;

    /**
     * 总反水
     */
    @Excel( name = "总反水" )
    private BigDecimal gamerevenve;

    /**
     * 会员盈利
     */
    @Excel( name = "会员盈利" )
    private BigDecimal gameprofit;

    /**
     * 平台盈利
     */
    @Excel( name = "平台盈利" )
    private BigDecimal plampfit;

    /**
     * 比例
     */
    @Excel( name = "比例" )
    private String bili;

    /**
     * 日期
     */
    @JsonFormat( pattern = "yyyy-MM-dd" )
    @Excel( name = "日期", width = 30, databaseFormat = "yyyy-MM-dd" )
    private String begindate;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "gameUuid", getGameUuid() )
                .append( "gameagent", getGameagent() )
                .append( "gameplame", getGameplame() )
                .append( "agentchild", getAgentchild() )
                .append( "agentchildname", getAgentchildname() )
                .append( "gamepepole", getGamepepole() )
                .append( "gametouzhu", getGametouzhu() )
                .append( "gamecell", getGamecell() )
                .append( "gamebet", getGamebet() )
                .append( "gamerevenve", getGamerevenve() )
                .append( "gameprofit", getGameprofit() )
                .append( "plampfit", getPlampfit() )
                .append( "bili", getBili() )
                .append( "begindate", getBegindate() )
                .toString();
    }
}
