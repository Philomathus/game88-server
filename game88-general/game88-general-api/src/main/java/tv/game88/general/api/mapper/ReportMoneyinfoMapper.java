package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.general.api.entity.ReportMoneyinfo;

import java.util.List;

/**
 * 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额Mapper接口
 *
 * @author 77tv
 * @date 2021-01-25
 */
public interface ReportMoneyinfoMapper extends BaseMapper<ReportMoneyinfo> {


    /**
     * 查询平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额列表
     *
     * @param reportMoneyinfo 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额
     *
     * @return 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额集合
     */
    public List<ReportMoneyinfo> selectReportMoneyinfoList( ReportMoneyinfo reportMoneyinfo );

    String calldataProrepPlamcom( @Param( "dateNowStr" ) String dateNowStr, @Param( "agentPlatform" ) String agentPlatform );


    ReportMoneyinfo countMoneyInfoData( ReportMoneyinfo reportMoneyinfo );
}