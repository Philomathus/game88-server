package tv.game88.platform.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.platform.api.entity.ActivityQuestType;

import java.util.List;

public interface ActivityQuestTypeMapper extends BaseMapper<ActivityQuestType> {
    /**
     * 查询任务类型列表
     *
     * @param activityQuestType 任务类型
     *
     * @return 任务类型集合
     */
    public List<ActivityQuestType> selectActivityQuestTypeList( ActivityQuestType activityQuestType );
}