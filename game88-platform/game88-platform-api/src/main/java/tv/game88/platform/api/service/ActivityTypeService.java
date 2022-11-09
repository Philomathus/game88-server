package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.ActivityType;

import java.util.List;

/**
 * 活动类型Service接口
 *
 * @author mengJun
 */
public interface ActivityTypeService extends IService<ActivityType> {
    /**
     * 查询活动类型列表
     *
     * @param activityType 活动类型
     *
     * @return 活动类型集合
     */
    public List<ActivityType> selectActivityTypeList( ActivityType activityType );
}