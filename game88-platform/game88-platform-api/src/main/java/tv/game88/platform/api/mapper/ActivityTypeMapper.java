package tv.game88.platform.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.platform.api.entity.ActivityType;

import java.util.List;

public interface ActivityTypeMapper extends BaseMapper<ActivityType> {
    /**
     * 查询活动类型列表
     *
     * @param activityType 活动类型
     *
     * @return 活动类型集合
     */
    public List<ActivityType> selectActivityTypeList( ActivityType activityType );
}