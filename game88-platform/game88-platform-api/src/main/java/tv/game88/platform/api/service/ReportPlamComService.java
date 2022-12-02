package tv.game88.platform.api.service;

import tv.game88.platform.api.entity.ReportPlamCom;

import java.util.List;


/**
 * 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间Service接口
 *
 * @author 77tv
 * @date 2021-01-25
 */
public interface ReportPlamComService {
	/**
	 * 查询综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间列表
	 *
	 * @param reportPlamCom 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间
	 * @return 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间集合
	 */
	public Object selectReportPlamComList( ReportPlamCom reportPlamCom);


	List<ReportPlamCom> exportPlamComList(ReportPlamCom reportPlamCom);
}