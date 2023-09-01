package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.general.api.entity.ReportPlamCom;

import java.util.List;


/**
 * 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间Mapper接口
 *
 * @author 77tv
 * @date 2021-01-25
 */
public interface ReportPlamComMapper extends BaseMapper<ReportPlamCom> {

    /**
     * 查询综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间列表
     *
     * @param reportPlamCom 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间
     *
     * @return 综合数据报会每天进行前一天数据的生成，如果需要查当天的数据则需手动调用prorep_plamcom报存储过程，传入当天时间集合
     */
    public List<ReportPlamCom> selectReportPlamComList( ReportPlamCom reportPlamCom );

    String calldataProrepPlamcom( @Param( "timedateta" ) String timedateta );

}