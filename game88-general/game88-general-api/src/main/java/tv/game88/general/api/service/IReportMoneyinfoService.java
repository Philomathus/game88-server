package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.entity.ReportMoneyinfo;

import java.text.ParseException;
import java.util.List;

/**
 * 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额Service接口
 *
 * @author 77tv
 * @date 2021-01-25
 */
public interface IReportMoneyinfoService extends IService<ReportMoneyinfo> {

    /**
     * 查询平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额列表
     *
     * @param reportMoneyinfo 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额
     *
     * @return 平台资金报，记录平台每日收入及支出总额，预估当前会员的积分余额集合
     */
    public Object selectReportMoneyinfoList( ReportMoneyinfo reportMoneyinfo ) throws ParseException;

    ReportMoneyinfo countMoneyData( ReportMoneyinfo reportMoneyinfo ) throws ParseException;

    List<ReportMoneyinfo> exportMoneyinfoList( ReportMoneyinfo reportMoneyinfo );
}