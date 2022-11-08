package tv.game88.platform.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.platform.api.entity.ActivityQuestInfo;

import java.util.List;

public interface ActivityQuestInfoMapper extends BaseMapper<ActivityQuestInfo> {
    /**
     * 查询任务信息列表
     *
     * @param activityQuestInfo 任务信息
     * @return 任务信息集合
     */
    public List<ActivityQuestInfo> selectActivityQuestInfoList( ActivityQuestInfo activityQuestInfo);
}