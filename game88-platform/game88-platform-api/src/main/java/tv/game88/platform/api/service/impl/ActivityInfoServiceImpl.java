package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.quest.entity.ActivityInfo;
import tv.game88.core.quest.mapper.ActivityInfoMapper;
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
        List<ActivityInfo> activityInfos = this.baseMapper.selectActivityInfoList( activityInfo );

        String domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        if ( !CollectionUtils.isEmpty( activityInfos ) ) {
            for ( ActivityInfo info : activityInfos ) {
                if ( StringUtils.isNotBlank( info.getIcon() ) && !info.getIcon().startsWith( "http" ) ) {
                    info.setIcon( domainValue + info.getIcon() );
                }
                if(info.getContent().contains( "domain.oss" )){
                    info.setContent( info.getContent().replaceAll( "\\$\\{domain\\.oss\\}", domainValue ) );
                }
            }
        }
        return activityInfos;
    }
}