package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.quest.entity.ActivityInfo;

import java.util.List;

/**
 * 活动信息Service接口
 *
 * @author mengJun
 */
public interface ActivityInfoService extends IService<ActivityInfo> {
	/**
	 * 查询活动信息列表
	 *
	 * @param activityInfo 活动信息
	 * @return 活动信息集合
	 */
	public List<ActivityInfo> selectActivityInfoList(ActivityInfo activityInfo);
}