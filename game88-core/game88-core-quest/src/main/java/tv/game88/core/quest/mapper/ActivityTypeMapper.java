package tv.game88.core.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.quest.entity.ActivityType;

import java.util.List;

public interface ActivityTypeMapper extends BaseMapper<ActivityType> {
    /**
     * 查询活动类型列表
     *
     * @param activityType 活动类型
     *
     * @return 活动类型集合
     */
    List<ActivityType> selectActivityTypeList( ActivityType activityType );
}