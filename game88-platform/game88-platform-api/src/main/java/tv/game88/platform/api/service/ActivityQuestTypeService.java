package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.ActivityQuestType;

import java.util.List;

/**
 * 任务类型Service接口
 *
 * @author MengJun
 */
public interface ActivityQuestTypeService extends IService<ActivityQuestType> {
    /**
     * 查询任务类型列表
     *
     * @param activityQuestType 任务类型
     *
     * @return 任务类型集合
     */
    public List<ActivityQuestType> selectActivityQuestTypeList( ActivityQuestType activityQuestType );
}