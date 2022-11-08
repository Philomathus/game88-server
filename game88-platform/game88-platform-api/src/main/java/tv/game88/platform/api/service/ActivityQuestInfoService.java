package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.ActivityQuestInfo;

import java.util.List;

/**
 * 任务信息Service接口
 *
 * @author MengJun
 */
public interface ActivityQuestInfoService extends IService<ActivityQuestInfo> {
	/**
	 * 查询任务信息列表
	 *
	 * @param activityQuestInfo 任务信息
	 * @return 任务信息集合
	 */
	public List<ActivityQuestInfo> selectActivityQuestInfoList( ActivityQuestInfo activityQuestInfo);
}