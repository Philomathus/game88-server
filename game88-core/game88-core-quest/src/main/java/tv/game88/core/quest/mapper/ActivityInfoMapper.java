package tv.game88.core.quest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.core.quest.entity.ActivityInfo;

import java.util.List;

public interface ActivityInfoMapper extends BaseMapper<ActivityInfo> {
    /**
     * 查询活动信息列表
     *
     * @param activityInfo 活动信息
     * @return 活动信息集合
     */
    public List<ActivityInfo> selectActivityInfoList(ActivityInfo activityInfo);
}