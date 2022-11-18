package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.game.api.base.BaseGameButt;
import tv.game88.game.api.base.GameButtFactoryUtil;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.dto.RspGameType;
import tv.game88.game.api.dto.RspGameTypes;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.service.GameService;
import tv.game88.game.api.service.MemberGameMoneyService;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Service
public class GameServiceImpl implements GameService {

    public static final BigDecimal ONE_HUNDRED = new BigDecimal( 100 );

    @Resource
    private RedisUtils             redisUtils;
    @Resource
    private GameCacheUtils         gameCacheUtils;
    @Resource
    private GameButtFactoryUtil    gameButtFactoryUtil;
    @Resource
    private MemberInfoMapper       memberInfoMapper;
    @Resource
    private MemberGameMoneyService memberGameMoneyService;

    @Value( "${spring.profiles.active}" )
    private String profile;

    @Override
    public RspGameTypes getGameTypes() {
        List<RspGameType> gameTypeList = gameCacheUtils.getEffectTypeList();
        RspGameTypes      rspGameTypes = new RspGameTypes();
        rspGameTypes.setRspGameTypes( gameTypeList );
        if ( !CollectionUtils.isEmpty( gameTypeList ) ) {
            Long typeId = gameTypeList.get( 0 ).getId();
            rspGameTypes.setRspGameInfos( gameCacheUtils.getEffectInfoList( typeId ) );
        }
        return rspGameTypes;
    }

    @Override
    public List<RspGameInfo> getGameInfoList( Long typeId ) {
        return gameCacheUtils.getEffectInfoList( typeId );
    }

    @Override
    public RspBase<?> joinGame( Long infoId, PlatformUser platformUser ) {
        GameInfo gameInfo = gameCacheUtils.getGameInfo( infoId );
        if ( gameInfo == null || !gameInfo.getEffect() ) {
            return RspBase.businessError( "该游戏不存在或已关闭" );
        }
        if ( gameInfo.getMaintain() && platformUser.getStatus() != 2 ) {
            return RspBase.businessError( "该游戏正在维护,请选择其他游戏" );
        }
        GamePlatform gamePlatform = gameCacheUtils.getGamePlatform( gameInfo.getPlatformId() );
        if ( gamePlatform == null || !gamePlatform.getEffect() ) {
            return RspBase.businessError( "该游戏不存在或已关闭" );
        }
        if ( gamePlatform.getMaintain() && platformUser.getStatus() != 2 ) {
            return RspBase.businessError( "该游戏正在维护,请选择其他游戏" );
        }

        if ( !redisUtils.lock( "joinGame" + platformUser.getId(), 20 ) ) {
            return RspBase.businessError( "正在进入游戏中.." );
        }

        BigDecimal userBalance = memberInfoMapper.getUserBalance( platformUser.getId() );
        BigDecimal changeMoney = userBalance.setScale( 0, RoundingMode.DOWN );
        // 测试号最多上分100块
        if ( platformUser.getStatus() == 2 && changeMoney.compareTo( ONE_HUNDRED ) > 0 ) {
            changeMoney = ONE_HUNDRED;
        }
        if ( changeMoney.compareTo( BigDecimal.ZERO ) < 0 ) {
            changeMoney = BigDecimal.ZERO;
        }
        String gameMemberId = profile + "_" + platformUser.getId();
        ReqJoinGame reqJoinGame = ReqJoinGame
                .builder()
                .des( AESCoder.decrypt( gamePlatform.getDes() ) )
                .md5( AESCoder.decrypt( gamePlatform.getMd5() ) )
                .agent( gamePlatform.getAgent() )
                .apiUrl( gamePlatform.getApiUrl() )
                .linecode( gamePlatform.getLinecode() )
                .kindId( gameInfo.getKindId() )
                .gameMemberId( gameMemberId )
                .memberId( platformUser.getId() )
                .transferMoney( changeMoney )
                .platformId( gamePlatform.getId() )
                .orderId( this.getGameOrderId( gameMemberId, gamePlatform.getAgent(), gamePlatform.getGameCategory() ) )
                .build();
        BaseGameButt baseGameButt = gameButtFactoryUtil.createGameButtProcessor( gamePlatform.getGameCategory() );
        try {
            // 获取token
            baseGameButt.getToken( reqJoinGame );
            // 创建账号
            baseGameButt.createAccount( reqJoinGame );
            // 扣除会员金额
            memberGameMoneyService.beginGameEnter( reqJoinGame );
            if ( changeMoney.compareTo( BigDecimal.ZERO ) > 0 ) {
                // 上分
                baseGameButt.transferMoney( reqJoinGame );
            }
            // 获取游戏链接
            String url = baseGameButt.getJoinGameUrl( reqJoinGame );
            redisUtils.unLock( "joinGame" + platformUser.getId() );
            return RspBase.ok( "获取游戏链接成功", url );
        } catch ( Exception e ) {
            log.error( "进入游戏失败,失败原因:" + e.getMessage(), e );
            redisUtils.unLock( "joinGame" + platformUser.getId() );
            return RspBase.businessError( "进入游戏失败,请重试" );
        }

    }

    private String getGameOrderId( String gameMemberId, String agent, EnumGameCategory gameCategory ) {
        return switch ( gameCategory ) {
            case AG -> agent
                    .concat( gameMemberId )
                    .concat( LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSS_FORMATTER ) );
            case BBIN -> gameMemberId + IdWorker.getIdStr();
            default -> agent
                    .concat( LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDDHHMMSSSSS_FORMATTER ) )
                    .concat( gameMemberId );
        };
    }
}
