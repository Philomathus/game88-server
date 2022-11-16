package tv.game88.core.member.manager;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.exception.NoMoneyException;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.member.entity.LogMoney;
import tv.game88.core.member.entity.MemberBcode;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.mapper.LogMoneyMapper;
import tv.game88.core.member.mapper.MemberBcodeMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Log4j2
public class MemberMoneyManager {

    @Resource
    private LogMoneyMapper    logMoneyMapper;
    @Resource
    private MemberBcodeMapper memberBcodeMapper;
    @Resource
    private MemberInfoMapper  memberInfoMapper;

    /**
     * 会员加钱
     *
     * @param userId    会员ID
     * @param addCount  增加的部分
     * @param enumMoney 交易类型
     * @param mult      打码倍数
     */
    @Transactional( rollbackFor = Exception.class )
    public void addMemberMoney( String userId, BigDecimal addCount, EnumMoney enumMoney, int mult, String mark,
                                String businessId, String markorder ) {
        if ( enumMoney.getType() < 0 ) {
            throw new BusinessException( "服务器异常" );
        }
        BigDecimal charge   = null;
        BigDecimal codeMult = null;
        //会员充值
        if ( enumMoney.getBcode() ) {
            charge   = addCount;
            codeMult = addCount.multiply( new BigDecimal( mult ) ).setScale( 2, RoundingMode.DOWN );
        }

        BigDecimal memberMoney = memberInfoMapper.getUserBalance( userId );

        memberInfoMapper.addMoneySelect( userId, addCount, charge, codeMult );
        //日志
        LogMoney log = new LogMoney();
        if ( StringUtils.isNotBlank( businessId ) ) {
            log.setId( businessId );
        } else {
            log.setId( IdWorker.get32UUID() );
        }
        log.setUserId( userId );
        log.setCreateTime( LocalDateTime.now() );
        log.setIncome( addCount );
        log.setPay( BigDecimal.ZERO );
        log.setType( enumMoney.getType() );
        log.setDes( enumMoney.getDes() );
        log.setMark( mark );
        log.setTotalBefore( memberMoney );
        log.setTotal( memberMoney.add( addCount ) );
        log.setMarkorder( markorder );
        logMoneyMapper.insert( log, log.getUserId().substring( log.getUserId().length() - 1 ) );
        //打码
        if ( enumMoney.getBcode() ) {
            MemberBcode code = new MemberBcode();
            code.setIncome( addCount );
            code.setCharge( addCount );
            code.setCreateTime( log.getCreateTime() );
            code.setCur( BigDecimal.ZERO );
            code.setStatus( 0 );
            code.setUserId( userId );
            code.setDes( enumMoney.getDes() );
            memberBcodeMapper.insert( code );
        }
    }

    /**
     * 会员资金扣减
     *
     * @param userId      用户ID
     * @param reduceMoney 扣减金额
     * @param enumMoney   资金类型枚举 扣减CODE必须为负数
     * @param mark        备注
     */
    @Transactional( rollbackFor = Exception.class )
    public void reduceMoney( String userId, BigDecimal reduceMoney, EnumMoney enumMoney, String mark ) {
        if ( enumMoney.getType() > 0 ) {
            throw new BusinessException( "服务器异常" );
        }
        BigDecimal memberMoney = memberInfoMapper.getUserBalance( userId );
        //扣减金额
        if ( memberInfoMapper.reduceMoney( userId, reduceMoney ) <= 0 ) {
            throw new NoMoneyException( "余额不足" );
        }
        //插入会员资金信息记录
        LogMoney log = new LogMoney();
        log.setId( IdWorker.get32UUID() );
        log.setUserId( userId );
        log.setCreateTime( LocalDateTime.now() );
        log.setIncome( BigDecimal.ZERO );
        log.setPay( reduceMoney );
        log.setType( enumMoney.getType() );
        log.setDes( enumMoney.getDes() );
        log.setMark( mark );
        log.setTotalBefore( memberMoney );
        log.setTotal( memberMoney.subtract( reduceMoney ) );
        logMoneyMapper.insert( log, log.getUserId().substring( log.getUserId().length() - 1 ) );
    }

    /**
     * 资金日志
     *
     * @param userId
     * @param reduceMoney
     */
    public int logSafebox( String userId, BigDecimal reduceMoney, String mark, BigDecimal totalBefore, BigDecimal total ) {
        //日志
        LogMoney log = new LogMoney();
        log.setId( IdWorker.get32UUID() );
        log.setUserId( userId );
        log.setCreateTime( LocalDateTime.now() );
        log.setIncome( BigDecimal.ZERO );
        log.setPay( reduceMoney );
        log.setType( EnumMoney.SAFE_BOX.getType() );
        log.setDes( EnumMoney.SAFE_BOX.getDes() );
        log.setMark( mark );
        log.setTotalBefore( totalBefore );
        log.setTotal( total );

        return logMoneyMapper.insert( log, log.getUserId().substring( log.getUserId().length() - 1 ) );
    }
}
