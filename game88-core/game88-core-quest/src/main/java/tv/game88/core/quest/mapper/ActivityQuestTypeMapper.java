package tv.game88.core.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.quest.entity.ActivityQuestType;

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