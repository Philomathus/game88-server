package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.ActivityQuestInfo;
import tv.game88.platform.api.mapper.ActivityQuestInfoMapper;
import tv.game88.platform.api.service.ActivityQuestInfoService;

import java.util.List;

/**
 * 任务信息Service业务层处理
 *
 * @author MengJun
 */
@Service
public class ActivityQuestInfoServiceImpl extends ServiceImpl<ActivityQuestInfoMapper, ActivityQuestInfo> implements ActivityQuestInfoService {
    /**
     * 查询任务信息列表
     *
     * @param activityQuestInfo 任务信息
     *
     * @return 任务信息
     */
    @Override
    public List<ActivityQuestInfo> selectActivityQuestInfoList( ActivityQuestInfo activityQuestInfo ) {
        return this.baseMapper.selectActivityQuestInfoList( activityQuestInfo );
    }
}