package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.game.api.dto.ReqJoinGame;
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
        String          primaryKey = reqJoinGame.getMemberId().concat( "_" ).concat( reqJoinGame.getPlatformId() + "" );
        MemberGameMoney gameMoney  = this.baseMapper.selectById( primaryKey );
        if ( gameMoney != null ) {
            gameMoney.setStatus( 0 ); // 开始状态
            gameMoney.setMoney( reqJoinGame.getTransferMoney() );
            gameMoney.setOrderId( reqJoinGame.getOrderId() );
            gameMoney.setCreateTime( LocalDateTime.now() );
            this.baseMapper.updateById( gameMoney );
        } else {
            gameMoney = new MemberGameMoney();
            gameMoney.setOrderId( reqJoinGame.getOrderId() );
            gameMoney.setId( primaryKey );
            gameMoney.setMoney( reqJoinGame.getTransferMoney() );
            gameMoney.setStatus( 0 );
            gameMoney.setMemberId( reqJoinGame.getMemberId() );
            gameMoney.setPlatformId( reqJoinGame.getPlatformId() );
            gameMoney.setCreateTime( LocalDateTime.now() );
            this.baseMapper.insert( gameMoney );
        }
        //扣款
        String mark = "上分为ID:" + reqJoinGame.getPlatformId() + "的游戏";
        memberMoneyManager.reduceMoney( reqJoinGame.getMemberId(), reqJoinGame.getTransferMoney(), EnumMoney.GAME_IN, mark );
        log.info( "游戏上分 - 扣款成功 - 会员:{},交易号:{},平台:{},上分金额:{}", reqJoinGame.getMemberId(), reqJoinGame.getOrderId(),
                reqJoinGame.getPlatformId(), reqJoinGame.getTransferMoney() );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void enterGameFail( ReqJoinGame reqJoinGame ) {
        MemberGameMoney update = new MemberGameMoney();
        update.setId( reqJoinGame.getMemberId().concat( "_" ).concat( reqJoinGame.getPlatformId() + "" ) );
        update.setStatus( -1 );
        update.setUpdateTime( LocalDateTime.now() );
        this.baseMapper.updateById( update );

        memberMoneyManager.addMemberMoney( reqJoinGame.getMemberId(), reqJoinGame.getTransferMoney(), EnumMoney.GAME_FAIL, 0,
                "游戏上分回退", null, reqJoinGame.getOrderId() );
        log.error( "进入游戏失败，回滚会员上分金额：userId:{},returnMoney:{}", reqJoinGame.getMemberId(), reqJoinGame.getTransferMoney() );
    }

    @Override
    public void enterGameSuccess( ReqJoinGame reqJoinGame ) {
        MemberGameMoney update = new MemberGameMoney();
        update.setId( reqJoinGame.getMemberId().concat( "_" ).concat( reqJoinGame.getPlatformId() + "" ) );
        update.setStatus( 1 );
        update.setUpdateTime( LocalDateTime.now() );
        this.baseMapper.updateById( update );
    }
}