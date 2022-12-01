package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.quest.entity.ActivityQuestInfo;
import tv.game88.core.quest.entity.MemberQuest;
import tv.game88.core.quest.mapper.ActivityQuestInfoMapper;
import tv.game88.core.quest.mapper.MemberQuestMapper;
import tv.game88.platform.api.cache.ActivityCacheUtil;
import tv.game88.platform.api.dto.RspActivityInfo;
import tv.game88.platform.api.dto.RspActivityType;
import tv.game88.platform.api.dto.RspQuestInfo;
import tv.game88.platform.api.dto.RspQuestType;
import tv.game88.platform.api.entity.ActivityInfo;
import tv.game88.platform.api.entity.ActivityQuestType;
import tv.game88.platform.api.entity.ActivityType;
import tv.game88.platform.api.service.ActivityService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Service
public class ActivityServiceImpl implements ActivityService {
    @Resource
    private RedisUtils              redisUtils;
    @Resource
    private ActivityCacheUtil       activityCacheUtil;
    @Resource
    private MemberQuestMapper       memberQuestMapper;
    @Resource
    private ActivityQuestInfoMapper activityQuestInfoMapper;
    @Resource
    private MemberMoneyManager      memberMoneyManager;

    @Override
    public List<RspActivityType> getActivityTypes( String token ) {
        List<ActivityType>    activeTypes = activityCacheUtil.getActiveTypes();
        List<RspActivityType> typeList    = new ArrayList<>();
        if ( activeTypes.size() > 0 ) {
            for ( ActivityType activityType : activeTypes ) {
                RspActivityType rspActivityType = new RspActivityType();
                rspActivityType.setName( activityType.getName() );
                rspActivityType.setId( activityType.getId() );
                typeList.add( rspActivityType );
            }
        }
        if ( typeList.size() > 0 ) {
            typeList.get( 0 ).setActivityList( this.getActivityInfos( activeTypes.get( 0 ).getId(), token ) );
        }
        return typeList;
    }

    @Override
    public List<RspActivityInfo> getActivityInfos( Long typeId, String token ) {
        List<RspActivityInfo> collect          = new ArrayList<>();
        List<ActivityInfo>    activeInfos      = activityCacheUtil.getActiveInfos();
        List<ActivityInfo>    activityInfoList = new ArrayList<>();
        if ( !CollectionUtils.isEmpty( activeInfos ) ) {
            activityInfoList = activeInfos
                    .stream()
                    .filter( activityInfo -> activityInfo.getTypeId().equals( typeId ) )
                    .collect( Collectors.toList() );
        }
        if ( !CollectionUtils.isEmpty( activityInfoList ) ) {
            String domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
            for ( ActivityInfo info : activityInfoList ) {
                RspActivityInfo rspActivityInfo = new RspActivityInfo();
                rspActivityInfo.setIcon( info.getIcon() );
                if ( StringUtils.isNotBlank( info.getIcon() ) && !info.getIcon().startsWith( "http" ) ) {
                    rspActivityInfo.setIcon( domainValue + info.getIcon() );
                }
                rspActivityInfo.setContent( info.getContent() );
                rspActivityInfo.setCreateTime( info.getCreateTime() );
                rspActivityInfo.setId( info.getId() );
                rspActivityInfo.setEffect( info.getEffect() );
                rspActivityInfo.setTitle( info.getTitle() );
                rspActivityInfo.setType( info.getType() );
                rspActivityInfo.setTypeId( info.getTypeId() );
                rspActivityInfo.setUrl( info.getUrl() );
                // 判断跳转链接
                if ( info.getType() != null && info.getType() == 1 && token != null ) {
                    String uriString = UriComponentsBuilder
                            .fromHttpUrl( info.getUrl() )
                            .queryParam( "token", token )
                            .build()
                            .toUriString();
                    rspActivityInfo.setUrl( uriString );
                }
                // 替换动态域名
                if ( info.getType() != null && info.getType() == 0 && StringUtils.isNotBlank( info.getContent() ) ) {
                    rspActivityInfo.setContent( info.getContent().replaceAll( "\\$\\{domain\\.oss\\}", domainValue ) );
                }
                collect.add( rspActivityInfo );
            }
        }
        return collect;
    }

    @Override
    public List<RspQuestType> getActivityQuestTypes( String memberId ) {
        List<ActivityQuestType> List     = activityCacheUtil.getQuestTypes();
        List<RspQuestType>      typeList = new ArrayList<>();
        if ( List.size() > 0 ) {
            for ( ActivityQuestType activityType : List ) {
                RspQuestType rspActivityType = new RspQuestType();
                rspActivityType.setName( activityType.getName() );
                rspActivityType.setId( activityType.getId() );
                typeList.add( rspActivityType );
            }
        }
        if ( typeList.size() > 0 ) {
            typeList.get( 0 ).setActivityList( getActivityQuestInfos( typeList.get( 0 ).getId(), memberId ) );
        }
        return typeList;
    }

    @Override
    public List<RspQuestInfo> getActivityQuestInfos( Long typeId, String memberId ) {
        List<ActivityQuestInfo> questInfos = activityCacheUtil.getQuestInfos();
        List<ActivityQuestInfo> lists      = new ArrayList<>();
        List<RspQuestInfo>      qlist      = new ArrayList<>();
        if ( !CollectionUtils.isEmpty( questInfos ) ) {
            lists = questInfos
                    .stream()
                    .filter( activityQuestInfo -> Objects.equals( activityQuestInfo.getTypeId(), typeId ) )
                    .collect( Collectors.toList() );
        }
        String domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        if ( !CollectionUtils.isEmpty( lists ) ) {
            for ( ActivityQuestInfo activityQuestInfo : lists ) {
                RspQuestInfo info = new RspQuestInfo();
                info.setContent( activityQuestInfo.getContent() );
                info.setGameTypeId( activityQuestInfo.getGameTypeId().intValue() );
                if ( StringUtils.isNotBlank( activityQuestInfo.getIcon() ) && !activityQuestInfo
                        .getIcon()
                        .startsWith( "http" ) ) {
                    info.setIcon( domainValue + activityQuestInfo.getIcon() );
                }
                info.setId( activityQuestInfo.getId() );
                info.setReward( activityQuestInfo.getReward() );
                info.setTarget( activityQuestInfo.getTarget() );
                info.setTitle( activityQuestInfo.getTitle() );
                qlist.add( info );
            }
        }
        //登录
        if ( !StringUtils.isEmpty( memberId ) ) {
            List<MemberQuest> memberQuest = new QueryChainWrapper<>( memberQuestMapper ).eq( "member_id", memberId ).list();
            if ( memberQuest.size() > 0 ) {
                Map<Long, MemberQuest> questMap = memberQuest
                        .stream()
                        .collect( Collectors.toMap( MemberQuest::getQuestId, Function.identity() ) );
                for ( RspQuestInfo q : qlist ) {
                    if ( questMap.containsKey( q.getId() ) ) {
                        q.setCurNum( questMap.get( q.getId() ).getCurNum() );
                        q.setStatus( questMap.get( q.getId() ).getStatus() );
                    }
                }
            }
        }
        return qlist;
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public RspBase<?> receiveQuestReward( Long questId, String memberId ) {
        if ( !redisUtils.lock( "receiveQuestReward" + memberId, 5 ) ) {
            throw new BusinessException( "请勿重复提交" );
        }
        MemberQuest memberQuest = new QueryChainWrapper<>( memberQuestMapper )
                .eq( "member_id", memberId )
                .eq( "quest_id", questId )
                .one();
        if ( memberQuest == null ) {
            return RspBase.businessError( "未达条件或任务过期" );
        }
        if ( memberQuest.getStatus() == 0 ) {
            throw new BusinessException( "未达领取条件" );
        }
        if ( memberQuest.getStatus() == 2 ) {
            return RspBase.businessError( "请勿重复领取" );
        }
        ActivityQuestInfo questInfo = activityQuestInfoMapper.selectById( questId );
        if ( questInfo == null ) {
            return RspBase.businessError( "未达条件或任务过期" );
        }
        MemberQuest update = new MemberQuest();
        update.setId( memberQuest.getId() );
        update.setStatus( 2 );
        if ( memberQuestMapper.updateById( update ) > 0 ) {
            String name    = questInfo.getContent() + "奖金:" + questInfo.getReward().toString();
            String orderId = "memberQuest-" + memberQuest.getId() + "-" + memberId;
            memberMoneyManager.addMemberMoney( memberId, questInfo.getReward(), EnumMoney.QUEST_BONUS, 1, name, orderId,
                    orderId );
            return RspBase.ok( "领取成功", questInfo.getReward() );
        }
        redisUtils.unLock( "receiveQuestReward" + memberId );
        throw new BusinessException( "领取失败,请重试" );
    }
}
