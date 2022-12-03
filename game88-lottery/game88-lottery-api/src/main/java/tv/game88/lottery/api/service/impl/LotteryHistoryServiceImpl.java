package tv.game88.lottery.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.utils.JsonUtil;
import tv.game88.core.lottery.entity.LotteryBet;
import tv.game88.core.lottery.mapper.LotteryBetMapper;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.manager.MemberMoneyManager;
import tv.game88.lottery.api.cache.LotteryCacheUtils;
import tv.game88.lottery.api.dto.HistoryResult;
import tv.game88.lottery.api.dto.RspLotteryInfo;
import tv.game88.lottery.api.entity.LotteryHistory;
import tv.game88.lottery.api.entity.LotteryTemp;
import tv.game88.lottery.api.mapper.LotteryHistoryMapper;
import tv.game88.lottery.api.mapper.LotteryTempMapper;
import tv.game88.lottery.api.service.LotteryHistoryService;
import tv.game88.lottery.api.utils.imserver.ImServerUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class LotteryHistoryServiceImpl extends ServiceImpl<LotteryHistoryMapper, LotteryHistory> implements LotteryHistoryService {
    @Resource
    private LotteryTempMapper  lotteryTempMapper;
    @Resource
    private MemberMoneyManager memberMoneyManager;

    @Resource
    private ImServerUtils imServerUtils;

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public List<LotteryHistory> selectLotteryHistoryList( LotteryHistory lotteryHistory ) {
        return this.baseMapper.selectLotteryHistoryList( lotteryHistory );
    }

    @Override
    public void newIssue( RspLotteryInfo lotteryInfo, String issue, LocalDateTime time, int i ) {
        String        issueId = issue + "-" + lotteryInfo.getId();
        LocalDateTime ktimes  = time.plusMinutes( 1 ).withSecond( 0 );
        if ( new QueryChainWrapper<>( this.baseMapper ).eq( "id", issueId ).count() <= 0 ) {
            LotteryHistory history = new LotteryHistory();
            history.setId( issueId );
            history.setLotteryId( lotteryInfo.getId() );
            history.setIssue( issue );
            history.setStatus( 0 );
            history.setKtime( ktimes );
            history.setName( lotteryInfo.getName() );
            history.setTotalBet( 0L );
            history.setTotalPrize( BigDecimal.ZERO );
            this.baseMapper.insert( history );
        }
        // 当前期
        if ( i == 0 ) {
            LotteryTemp lotteryTemp = LotteryCacheUtils.me.getLotteryTemp( lotteryInfo.getId() );
            if ( lotteryTemp == null ) {
                lotteryTemp = new LotteryTemp();
                lotteryTemp.setId( lotteryInfo.getId() );
                lotteryTemp.setIssue( issue );
                lotteryTemp.setKtime( ktimes );
                lotteryTempMapper.insert( lotteryTemp );
            } else {
                lotteryTemp.setIssueJust( lotteryTemp.getIssue() );
                lotteryTemp.setIssue( issue );
                lotteryTemp.setKtime( ktimes );
                lotteryTempMapper.updateById( lotteryTemp );
            }
            LotteryCacheUtils.me.setLotteryTemp( lotteryTemp );

            this.sendNewLotteryIssueMsg( lotteryInfo.getId(), ktimes, issue );
        }
    }

    private void sendNewLotteryIssueMsg( Integer lotteryId, LocalDateTime ktimes, String issue ) {
        LocalDateTime now     = LocalDateTime.now();
        long          cutDown = now.until( ktimes, ChronoUnit.SECONDS );
        if ( cutDown < 0 ) {
            return;
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put( "id", lotteryId );
        data.put( "cutDown", cutDown );
        data.put( "curIssue", issue );
        data.put( "at", 1 );//1= 开新期2=投注  3=中奖

        HashMap<String, Object> ext = new HashMap<>();
        ext.put( "type", 101 );
        ext.put( "data", data );
        // 1 =新开启 2 = 投注, 中奖
        ext.put( "act", 1 );
        imServerUtils.sendGroupMessage( lotteryId + "", ext );
    }

    @Override
    @Transactional( rollbackFor = Exception.class )
    public void awardByLotteryResult( List<LotteryBet> updateList, Map<String, BigDecimal> prizeMap, String historyId,
                                      Map<String, BigDecimal> nowMoney, String lotteryName ) {
        long             now     = System.currentTimeMillis();
        SqlSession       session = sqlSessionTemplate.getSqlSessionFactory().openSession( ExecutorType.BATCH, false );
        LotteryBetMapper mapper  = session.getMapper( LotteryBetMapper.class );
        int              count   = 0;
        for ( LotteryBet update : updateList ) {
            String substring = update.getMemberId().substring( update.getMemberId().length() - 1 );
            mapper.updateStatusPrize( update.getId(), substring, update.getPrize(), update.getCode(), update.getUpdateTime(),
                    update.getStatus() );
            count += 1;
            if ( count >= 200 ) {
                session.commit();
                count = 0;
            }
        }
        if ( count > 0 ) {
            session.commit();
        }
        session.close();

        for ( String memberId : prizeMap.keySet() ) {
            memberMoneyManager.addMemberMoney( memberId, prizeMap.get( memberId ), EnumMoney.LOTTERY_BONUS, 0,
                    lotteryName + "派奖", historyId.concat( "-" ).concat( memberId ), historyId );
        }
        log.info( "awardPrize更新投注状态条数：{},执行时间:{}ms", updateList.size(), System.currentTimeMillis() - now );
    }

    @Override
    public List<HistoryResult> selectResultWaite( String lotteryAgent, Integer lotteryId ) {
        List<HistoryResult> list = baseMapper.selectResultWaite( lotteryAgent, lotteryId );
        if ( list.size() == 0 ) {
            return list;
        }
        List<LotteryHistory> upList = new ArrayList<>();
        for ( HistoryResult r : list ) {
            LotteryHistory h = new LotteryHistory();
            h.setId( r.getId() );
            h.setCode( r.getCode() );
            h.setStatus( 1 );
            h.setAnalyse( r.getAnalyse() );
            upList.add( h );
        }
        this.updateBatchById( upList );
        log.error( "lotteryId:{} - 抓奖 - {}", lotteryId, JsonUtil.object2Json( list ) );
        return list;
    }
}
