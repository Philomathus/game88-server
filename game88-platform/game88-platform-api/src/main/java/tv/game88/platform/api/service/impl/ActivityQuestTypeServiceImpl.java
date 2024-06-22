package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.core.quest.entity.ActivityQuestType;
import tv.game88.core.quest.mapper.ActivityQuestTypeMapper;
import tv.game88.platform.api.service.ActivityQuestTypeService;

import java.util.List;

/**
 * 任务类型Service业务层处理
 *
 * @author MengJun
 */
@Service
public class ActivityQuestTypeServiceImpl extends ServiceImpl<ActivityQuestTypeMapper, ActivityQuestType> implements ActivityQuestTypeService {
    /**
     * 查询任务类型列表
     *
     * @param activityQuestType 任务类型
     *
     * @return 任务类型
     */
    @Override
    public List<ActivityQuestType> selectActivityQuestTypeList( ActivityQuestType activityQuestType ) {
        return this.baseMapper.selectActivityQuestTypeList( activityQuestType );
    }
}