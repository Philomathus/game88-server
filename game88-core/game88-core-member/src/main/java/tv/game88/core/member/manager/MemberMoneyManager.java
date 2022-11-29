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
import tv.game88.core.member.cache.ConfigVipCacheUtils;
import tv.game88.core.member.entity.ConfigVip;
import tv.game88.core.member.entity.LogMoney;
import tv.game88.core.member.entity.MemberBcode;
import tv.game88.core.member.enums.EnumMoney;
import tv.game88.core.member.mapper.LogMoneyMapper;
import tv.game88.core.member.mapper.MemberBcodeMapper;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.core.member.vo.PlatformUser;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class MemberMoneyManager {
    @Resource
    private RedisUtils          redisUtils;
    @Resource
    private LogMoneyMapper      logMoneyMapper;
    @Resource
    private MemberBcodeMapper   memberBcodeMapper;
    @Resource
    private MemberInfoMapper    memberInfoMapper;
    @Resource
    private ConfigVipCacheUtils configVipCacheUtils;

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

        if ( enumMoney == EnumMoney.COMMISSION ) {
            memberInfoMapper.addInviterMoney( userId, addCount );
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

        // 充值加会员等级
        if ( Arrays.asList( 1, 2, 3 ).contains( enumMoney.getType() ) ) {
            this.checkAndUpdateVip( userId );
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

    private void checkAndUpdateVip( String memberId ) {
        BigDecimal userCharge = memberInfoMapper.getUserCharge( memberId );
        Integer    userVip    = memberInfoMapper.getUserVip( memberId );
        List<ConfigVip> configVips = configVipCacheUtils
                .getConfigVipMap()
                .values()
                .stream()
                .sorted( Comparator.comparing( ConfigVip::getBcode ) )
                .toList();
        Integer vip = 1;
        for ( ConfigVip configVip : configVips ) {
            if ( userCharge.compareTo( configVip.getBcode() ) < 0 ) {
                break;
            }
            vip = configVip.getLevel();
        }
        if ( userVip >= vip ) {
            return;
        }
        memberInfoMapper.updateVipById( memberId, vip );

        // 更新缓存
        String token = redisUtils.strGet( Constants.MEMBER_LOGIN_USER + memberId );
        if ( StringUtils.isNotBlank( token ) && redisUtils.exists( Constants.MEMBER_LOGIN_TOKEN + token ) ) {
            Map loginUserMap = redisUtils.hGetAll( Constants.MEMBER_LOGIN_TOKEN + token );
            PlatformUser platformUser = JsonUtil.json2Object( loginUserMap
                    .getOrDefault( "platformUserStr", "" )
                    .toString(), PlatformUser.class );
            platformUser.setVip( vip );
            loginUserMap.put( "platformUserStr", JsonUtil.object2Json( platformUser ) );
            redisUtils.hMSet( Constants.MEMBER_LOGIN_TOKEN + token, loginUserMap );
        }
    }
}
