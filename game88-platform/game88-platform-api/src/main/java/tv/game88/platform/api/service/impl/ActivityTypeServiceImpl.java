package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.ActivityType;
import tv.game88.platform.api.mapper.ActivityTypeMapper;
import tv.game88.platform.api.service.ActivityTypeService;

import java.util.List;

/**
 * 活动类型Service业务层处理
 *
 * @author mengJun
 */
@Service
public class ActivityTypeServiceImpl extends ServiceImpl<ActivityTypeMapper, ActivityType> implements ActivityTypeService {
    /**
     * 查询活动类型列表
     *
     * @param activityType 活动类型
     *
     * @return 活动类型
     */
    @Override
    public List<ActivityType> selectActivityTypeList( ActivityType activityType ) {
        return this.baseMapper.selectActivityTypeList( activityType );
    }
}