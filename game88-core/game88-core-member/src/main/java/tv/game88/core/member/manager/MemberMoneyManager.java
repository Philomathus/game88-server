package tv.game88.core.member.manager;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.exception.NoMoneyException;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.member.entity.*;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.mapper.LogMoneyMapper;
import tv.game88.core.member.mapper.MemberBcodeMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class MemberMoneyManager {
    @Resource
    private RedisUtils        redisUtils;
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
    public void addMemberMoney( String userId, BigDecimal addCount, EnumMoney enumMoney, BigDecimal mult, String mark,
                                String businessId, String markorder ) {
        if ( enumMoney.getType() < 0 ) {
            throw new BusinessException( "服务器异常" );
        }
        BigDecimal charge   = null;
        BigDecimal codeMult = null;
        //会员充值
        if ( enumMoney.getBcode() && mult.compareTo( BigDecimal.ZERO ) > 0 ) {
            charge   = addCount;
            codeMult = addCount.multiply( mult ).setScale( 2, RoundingMode.DOWN );
        }

        BigDecimal memberMoney = memberInfoMapper.getUserBalance( userId );

        int addMoney = memberInfoMapper.addMoneySelect( userId, addCount, charge, codeMult );

        int addInviterMoney = 1;
        if ( enumMoney == EnumMoney.COMMISSION ) {
            addInviterMoney = memberInfoMapper.addInviterMoney( userId, addCount );
        }

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
        int insertLogMoney = logMoneyMapper.insert( log, log.getUserId().substring( log.getUserId().length() - 1 ) );
        //打码
        int insertBcode = 1;
        if ( enumMoney.getBcode() && codeMult != null && codeMult.compareTo( BigDecimal.ZERO ) > 0 ) {
            MemberBcode code = new MemberBcode();
            code.setIncome( codeMult );
            code.setCharge( addCount );
            code.setCreateTime( log.getCreateTime() );
            code.setCur( BigDecimal.ZERO );
            code.setStatus( 0 );
            code.setUserId( userId );
            code.setDes( enumMoney.getDes() );
            insertBcode = memberBcodeMapper.insert( code );
        }
        if ( addMoney <= 0 || addInviterMoney <= 0 || insertLogMoney <= 0 || insertBcode <= 0 ) {
            throw new BusinessException( "资金记入失败,请重试" );
        }
    }

    @Transactional( rollbackFor = Exception.class )
    public void addMemberMoneyStarSend( MemberMoney memberMoney, MemberInfo memberInfo, String markOrder, String adminName,
                                        String moneyDes ) {
        String userId   = memberMoney.getId();
        String userName = memberInfo.getUserName();
        String mark     = moneyDes + ",操作人:" + adminName;

        BigDecimal trade    = memberMoney.getMoney();
        BigDecimal totalOld = memberInfo.getAccountNow();
        BigDecimal totalNow = totalOld.add( trade );

        BigDecimal codeMult  = trade.multiply( memberMoney.getBeat() ).setScale( 2, RoundingMode.DOWN );
        int        hasIncome = trade.compareTo( BigDecimal.ZERO );
        if ( hasIncome == 0 ) {
            return;
        }

        int addMemberInfo = memberInfoMapper.addMoneySelect( userId, trade, null, codeMult );

        LogMoney logMoney = new LogMoney();
        logMoney.setId( markOrder );
        logMoney.setUserId( userId );
        logMoney.setUserName( userName );
        logMoney.setCreateTime( LocalDateTime.now() );
        logMoney.setIncome( hasIncome > 0 ? trade : BigDecimal.ZERO );
        logMoney.setPay( hasIncome < 0 ? trade.negate() : BigDecimal.ZERO );
        logMoney.setType( EnumMoney.WONGIVE.getType() );
        logMoney.setDes( EnumMoney.WONGIVE.getDes() );
        logMoney.setMark( mark );
        logMoney.setTotalBefore( totalOld );
        logMoney.setTotal( totalNow );
        logMoney.setMarkorder( markOrder );
        int insertLogMoney = logMoneyMapper.insert( logMoney, userId.substring( userId.length() - 1 ) );

        MemberBcode code = new MemberBcode();
        code.setCharge( trade );
        code.setIncome( codeMult );
        code.setCreateTime( logMoney.getCreateTime() );
        code.setCur( BigDecimal.ZERO );
        code.setStatus( 0 );
        code.setUserId( userId );
        code.setDes( moneyDes );
        int insertBCode = memberBcodeMapper.insert( code );
        if ( addMemberInfo <= 0 || insertLogMoney <= 0 || insertBCode <= 0 ) {
            throw new BusinessException( "资金记入失败,请重试" );
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
        int insertLogMoney = logMoneyMapper.insert( log, log.getUserId().substring( log.getUserId().length() - 1 ) );
        if ( insertLogMoney <= 0 ) {
            throw new BusinessException( "资金日志记入失败,请重试" );
        }
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

    public void checkAndUpdateVip( String memberId, List<ConfigVip> configVips ) {
        BigDecimal codeTotal = memberInfoMapper.getUserCodeTotal( memberId );
        Integer    userVip   = memberInfoMapper.getUserVip( memberId );
        Integer    vip       = 1;
        for ( ConfigVip configVip : configVips ) {
            if ( codeTotal.compareTo( configVip.getBcode() ) < 0 ) {
                break;
            }
            vip = configVip.getLevel();
        }
        if ( userVip >= vip ) {
            return;
        }
        int updateVip = memberInfoMapper.updateVipById( memberId, vip );
        if ( updateVip > 0 ) {
            // 更新缓存
            String token = redisUtils.strGet( Constants.MEMBER_LOGIN_USER + memberId );
            if ( StringUtils.isNotBlank( token ) && redisUtils.exists( Constants.MEMBER_LOGIN_TOKEN + token ) ) {
                Map loginUserMap = redisUtils.hGetAll( Constants.MEMBER_LOGIN_TOKEN + token );
                PlatformUser platformUser = JsonUtil.json2Object( loginUserMap.getOrDefault( "platformUserStr", "" )
                                                                              .toString(), PlatformUser.class );
                platformUser.setVip( vip );
                loginUserMap.put( "platformUserStr", JsonUtil.object2Json( platformUser ) );
                redisUtils.hMSet( Constants.MEMBER_LOGIN_TOKEN + token, loginUserMap );
            }
        }
    }
}
