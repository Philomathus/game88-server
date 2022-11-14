package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.quest.entity.ActivityQuestInfo;
import tv.game88.core.quest.mapper.ActivityQuestInfoMapper;
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
        List<ActivityQuestInfo> activityQuestInfos = this.baseMapper.selectActivityQuestInfoList( activityQuestInfo );
        String                domainValue      = ConfigDomainCacheUtil.me.getDomainOssValue();
        if ( !CollectionUtils.isEmpty( activityQuestInfos ) ) {
            for ( ActivityQuestInfo info : activityQuestInfos ) {
                if ( StringUtils.isNotBlank( info.getIcon() ) && !info.getIcon().startsWith( "http" ) ) {
                    info.setIcon( domainValue + info.getIcon() );
                }
            }
        }
        return activityQuestInfos;
    }
}