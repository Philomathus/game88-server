package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.entity.ActivityInfo;
import tv.game88.platform.api.mapper.ActivityInfoMapper;
import tv.game88.platform.api.service.ActivityInfoService;

import java.util.List;

/**
 * 活动信息Service业务层处理
 *
 * @author mengJun
 */
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo> implements ActivityInfoService {
    /**
     * 查询活动信息列表
     *
     * @param activityInfo 活动信息
     * @return 活动信息
     */
    @Override
    public List<ActivityInfo> selectActivityInfoList(ActivityInfo activityInfo) {
        return this.baseMapper.selectActivityInfoList(activityInfo);
    }
}