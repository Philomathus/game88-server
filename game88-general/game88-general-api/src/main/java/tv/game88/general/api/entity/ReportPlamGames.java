package tv.game88.general.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import tv.game88.common.vo.BaseEntity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 【请填写功能名称】对象 report_plam_games
 *
 * @author 77tv
 * @date 2021-01-26
 */
@EqualsAndHashCode( callSuper = true )
@Data
public class ReportPlamGames extends BaseEntity implements Serializable  {
	private static final long serialVersionUID = 1L;

	/** id */
	private String gameUuid;

	/** 平台编号 */
	@Excel( name = "平台编号" )
	private String gameagent;

	/** 平台名称 */
	@Excel( name = "平台名称" )
	private String gameplame;

	/** 投注人数 */
	@Excel( name = "投注人数" )
	private BigDecimal gamepepole;

	/** 投注比数 */
	@Excel( name = "投注比数" )
	private BigDecimal gametouzhu;

	/** 总投注金额 */
	@Excel( name = "总投注金额" )
	private BigDecimal gamecell;

	/** 有效投注金额 */
	@Excel( name = "有效投注金额" )
	private BigDecimal gamebet;

	/** 总反水 */
	@Excel( name = "总反水" )
	private BigDecimal gamerevenve;

	/** 会员盈利 */
	@Excel( name = "会员盈利" )
	private BigDecimal gameprofit;

	/** 平台盈利 */
	@Excel( name = "平台盈利" )
	private BigDecimal plampfit;

	/** 比例 */
	@Excel( name = "比例" )
	private String bili;

	private String agentPlatform;

	private String agentPlatformLive;

	/** 日期 */

	@Excel( name = "日期" )
	private String     begindate;
	private String endDate;
	private Integer    countBetPeople;
	private BigDecimal countBetMoney;
	private BigDecimal memberProfit;
	private String dateTime;

	@Override
	public String toString() {
		return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
				.append( "gameUuid", getGameUuid() )
				.append( "gameagent", getGameagent() )
				.append( "gameplame", getGameplame() )
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