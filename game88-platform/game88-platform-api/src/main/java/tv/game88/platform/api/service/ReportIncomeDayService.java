package tv.game88.platform.api.service;

import tv.game88.platform.api.entity.ReportIncomeDay;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author 77tv
 * @date 2021-01-26
 */
public interface ReportIncomeDayService {



	/**
	 * 查询【请填写功能名称】列表
	 *
	 * @param reportIncomeDay 【请填写功能名称】
	 * @return 【请填写功能名称】集合
	 */
	public List<ReportIncomeDay> selectReportIncomeDayList( ReportIncomeDay reportIncomeDay);

    ReportIncomeDay countSuccessData(ReportIncomeDay reportIncomeDay);
}