package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.game.api.cache.GameCacheUtils;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.entity.GamePlatform;
import tv.game88.game.api.entity.MemberGameMoney;
import tv.game88.game.api.mapper.MemberGameMoneyMapper;
import tv.game88.game.api.service.MemberGameMoneyService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Log4j2
@Service
public class MemberGameMoneyServiceImpl extends ServiceImpl<MemberGameMoneyMapper, MemberGameMoney> implements MemberGameMoneyService {
    @Resource
    private MemberMoneyManager memberMoneyManager;
    @Resource
    private GameCacheUtils     gameCacheUtils;

    @Override
    public String selectMaxGameOrderCode( Long platformId ) {
        return this.baseMapper.selectMaxGameOrderCode( platformId );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void beginGameEnter( ReqJoinGame reqJoinGame ) {
        if ( reqJoinGame.getTransferMoney().compareTo( BigDecimal.ZERO ) <= 0 ) {
            log.error( "游戏上分 - 无需扣款 - 会员:{},交易号:{},平台:{},上分金额小于等于0", reqJoinGame.getMemberId(), reqJoinGame.getOrderId(),
                    reqJoinGame.getPlatformId() );
            return;
        }
        MemberGameMoney gameMoney = new MemberGameMoney();
        gameMoney.setOrderId( reqJoinGame.getOrderId() );
        gameMoney.setMoney( reqJoinGame.getTransferMoney() );
        gameMoney.setStatus( 0 );
        gameMoney.setMemberId( reqJoinGame.getMemberId() );
        gameMoney.setPlatformId( reqJoinGame.getPlatformId() );
        gameMoney.setCreateTime( LocalDateTime.now() );
        this.baseMapper.insert( gameMoney );
        //扣款
        GamePlatform gamePlatform = gameCacheUtils.getGamePlatform( reqJoinGame.getPlatformId() );
        String       mark         = "上分" + gamePlatform.getName() + "游戏";
        memberMoneyManager.reduceMoney( reqJoinGame.getMemberId(), reqJoinGame.getTransferMoney(), EnumMoney.GAME_IN, mark );
        log.info( "游戏上分 - 扣款成功 - 会员:{},交易号:{},平台:{},上分金额:{}", reqJoinGame.getMemberId(), reqJoinGame.getOrderId(),
                reqJoinGame.getPlatformId(), reqJoinGame.getTransferMoney() );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void enterGameFail( ReqJoinGame reqJoinGame ) {
        MemberGameMoney update = new MemberGameMoney();
        update.setOrderId( reqJoinGame.getOrderId() );
        update.setStatus( 1 );
        update.setUpdateTime( LocalDateTime.now() );
        this.baseMapper.updateById( update );

        memberMoneyManager.addMemberMoney( reqJoinGame.getMemberId(), reqJoinGame.getTransferMoney(), EnumMoney.GAME_FAIL, 0,
                "游戏上分回退", null, reqJoinGame.getOrderId() );
        log.error( "进入游戏失败，回滚会员上分金额：userId:{},returnMoney:{}", reqJoinGame.getMemberId(), reqJoinGame.getTransferMoney() );
    }

    @Override
    public void enterGameSuccess( ReqJoinGame reqJoinGame ) {
        MemberGameMoney update = new MemberGameMoney();
        update.setOrderId( reqJoinGame.getOrderId() );
        update.setStatus( 2 );
        update.setUpdateTime( LocalDateTime.now() );
        this.baseMapper.updateById( update );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void outGameSuccess( ReqJoinGame reqJoinGame ) {
        if ( reqJoinGame.getTransferMoney().compareTo( BigDecimal.ZERO ) <= 0 ) {
            log.error( "游戏下分 - 无需加分 - 会员:{},交易号:{},平台:{},上分金额小于等于0", reqJoinGame.getMemberId(), reqJoinGame.getOrderId(),
                    reqJoinGame.getPlatformId() );
            return;
        }
        MemberGameMoney gameMoney = new MemberGameMoney();
        gameMoney.setOrderId( reqJoinGame.getOrderId() );
        gameMoney.setMoney( reqJoinGame.getTransferMoney() );
        gameMoney.setStatus( 4 );
        gameMoney.setMemberId( reqJoinGame.getMemberId() );
        gameMoney.setPlatformId( reqJoinGame.getPlatformId() );
        gameMoney.setCreateTime( LocalDateTime.now() );
        this.baseMapper.insert( gameMoney );
        GamePlatform gamePlatform = gameCacheUtils.getGamePlatform( reqJoinGame.getPlatformId() );
        memberMoneyManager.addMemberMoney( reqJoinGame.getMemberId(), reqJoinGame.getTransferMoney(), EnumMoney.GAME_OUT, 0,
                "游戏下分," + gamePlatform.getName(), null, reqJoinGame.getOrderId() );
        log.info( "游戏下分成功，会员游戏下分金额：userId:{},returnMoney:{}", reqJoinGame.getMemberId(), reqJoinGame.getTransferMoney() );
    }

    @Override
    public void outGameFail( ReqJoinGame reqJoinGame ) {
        MemberGameMoney gameMoney = new MemberGameMoney();
        gameMoney.setOrderId( reqJoinGame.getOrderId() );
        gameMoney.setMoney( reqJoinGame.getTransferMoney() );
        gameMoney.setStatus( 3 );
        gameMoney.setMemberId( reqJoinGame.getMemberId() );
        gameMoney.setPlatformId( reqJoinGame.getPlatformId() );
        gameMoney.setCreateTime( LocalDateTime.now() );
        this.baseMapper.insert( gameMoney );
    }
}