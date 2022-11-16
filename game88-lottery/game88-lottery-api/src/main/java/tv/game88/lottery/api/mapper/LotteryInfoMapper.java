package tv.game88.lottery.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.lottery.api.entity.LotteryInfo;

import java.util.List;

/**
 * 彩票信息Mapper接口
 *
 * @author mengJun
 */
public interface LotteryInfoMapper extends BaseMapper<LotteryInfo> {

	/**
	 * 查询彩票信息列表
	 *
	 * @param lotteryInfo 彩票信息
	 * @return 彩票信息集合
	 */
	public List<LotteryInfo> selectLotteryInfoList(LotteryInfo lotteryInfo);
}