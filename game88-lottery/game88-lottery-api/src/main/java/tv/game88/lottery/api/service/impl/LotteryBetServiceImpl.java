package tv.game88.lottery.api.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.vo.RspBase;
import tv.game88.core.lottery.entity.LotteryBet;
import tv.game88.core.lottery.mapper.LotteryBetMapper;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.lottery.api.dto.ReqBet;
import tv.game88.lottery.api.dto.RspBet;
import tv.game88.lottery.api.service.LotteryBetService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 彩票会员下注详情Service业务层处理
 *
 * @author mengJun
 */
@Service
public class LotteryBetServiceImpl extends ServiceImpl<LotteryBetMapper, LotteryBet> implements LotteryBetService {
    @Resource
    private MemberMoneyManager memberMoneyManager;
    @Resource
    private MemberInfoMapper   memberInfoMapper;

    /**
     * 查询彩票会员下注详情列表
     *
     * @param lotteryBet 彩票会员下注详情
     *
     * @return 彩票会员下注详情
     */
    @Override
    public List<LotteryBet> selectLotteryBetList( LotteryBet lotteryBet ) {
        return this.baseMapper.selectLotteryBetList( lotteryBet );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public RspBase<RspBet> userBet( PlatformUser platformUser, ReqBet reqBet, String[] bet_select, String lotteryName,
                                    String issue, BigDecimal cost ) {
        memberMoneyManager.reduceMoney( platformUser.getId(), cost, EnumMoney.LOTTERY_BET, lotteryName + "投注" );

        String idLatest = platformUser.getId().substring( platformUser.getId().length() - 1 );

        RspBet rspBet = new RspBet();
        rspBet.setMoney( memberInfoMapper.getUserBalance( platformUser.getId() ) );
        rspBet.setBetId( reqBet.getBetIds() );
        rspBet.setLotteryId( reqBet.getLotteryId() );
        rspBet.setChip( reqBet.getChip() );
        rspBet.setIssue( issue );
        RspBase<RspBet> rspBase = new RspBase<>();
        rspBase.setData( rspBet );

        LotteryBet db = new LotteryBet();
        db.setId( IdWorker.get32UUID() );
        db.setLotteryId( reqBet.getLotteryId() );
        db.setMethodId( reqBet.getMethodId() );
        db.setIssue( issue );
        db.setMemberId( platformUser.getId() );
        db.setMemberStatus( platformUser.getStatus() );
        db.setStatus( 0 );
        db.setAnchor( reqBet.getAnchor() );
        db.setLotteryName( lotteryName );
        db.setBetSelect( String.join( "&", bet_select ) );
        db.setCost( cost );
        db.setChip( new BigDecimal( reqBet.getChip() ) );
        db.setBetIds( reqBet.getBetIds() );
        db.setBetTime( LocalDateTime.now() );
        this.baseMapper.insertLotteryBet( db, idLatest );
        return rspBase;
    }

    @Override
    public String procherckQuzhiImport( Integer lotteryId ) {
        String out = "";
        return this.baseMapper.procherckQuzhiImport( lotteryId, out );
    }
}