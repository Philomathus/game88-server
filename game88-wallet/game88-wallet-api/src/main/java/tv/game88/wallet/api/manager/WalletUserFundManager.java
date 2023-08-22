package tv.game88.wallet.api.manager;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.exception.NoMoneyException;
import tv.game88.common.utils.StringUtils;
import tv.game88.wallet.api.entity.WalletFundLog;
import tv.game88.wallet.api.mapper.WalletFundLogMapper;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.type.WalletUserFundEnum;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Log4j2
public class WalletUserFundManager {
    @Resource
    private WalletFundLogMapper walletFundLogMapper;
    @Resource
    private WalletUserMapper    walletUserMapper;

    /**
     * 会员加钱
     *
     * @param userId   会员ID
     * @param addMoney 增加的部分
     * @param fundEnum 交易类型
     */
    @Transactional( rollbackFor = Exception.class )
    public void addWalletUserMoney( String userId, BigDecimal addMoney, WalletUserFundEnum fundEnum, String mark,
                                    String businessId, String markorder ) {
        if ( fundEnum.getType() < 0 ) {
            throw new BusinessException( "逻辑异常" );
        }
        BigDecimal userBalance = walletUserMapper.getUserMoney( userId );

        int updateMoney = walletUserMapper.addMoney( userId, addMoney );

        //日志
        WalletFundLog log = new WalletFundLog();
        if ( StringUtils.isNotBlank( businessId ) ) {
            log.setId( businessId );
        } else {
            log.setId( IdWorker.get32UUID() );
        }
        log.setUserId( userId );
        log.setCreateTime( LocalDateTime.now() );
        log.setIncome( addMoney );
        log.setPay( BigDecimal.ZERO );
        log.setType( fundEnum.getType() );
        log.setDes( fundEnum.getDes() );
        log.setMark( mark );
        log.setTotalBefore( userBalance );
        log.setTotal( userBalance.add( addMoney ) );
        log.setMarkorder( markorder );
        int insertLogMoney = walletFundLogMapper.insert( log );
        if ( updateMoney <= 0 || insertLogMoney <= 0 ) {
            throw new BusinessException( "资金日志记入失败,请重试" );
        }
    }

    /**
     * 会员资金扣减
     *
     * @param userId      用户ID
     * @param reduceMoney 扣减金额
     * @param fundEnum    资金类型枚举 扣减CODE必须为负数
     * @param mark        备注
     */
    @Transactional( rollbackFor = Exception.class )
    public void reduceWalletUserMoney( String userId, BigDecimal reduceMoney, WalletUserFundEnum fundEnum, String mark ) {
        if ( fundEnum.getType() > 0 ) {
            throw new BusinessException( "逻辑异常" );
        }
        BigDecimal userMoney = walletUserMapper.getUserMoney( userId );
        //扣减金额
        if ( walletUserMapper.reduceMoney( userId, reduceMoney ) <= 0 ) {
            throw new NoMoneyException( "余额不足" );
        }
        //插入会员资金信息记录
        WalletFundLog log = new WalletFundLog();
        log.setId( IdWorker.get32UUID() );
        log.setUserId( userId );
        log.setCreateTime( LocalDateTime.now() );
        log.setIncome( BigDecimal.ZERO );
        log.setPay( reduceMoney );
        log.setType( fundEnum.getType() );
        log.setDes( fundEnum.getDes() );
        log.setMark( mark );
        log.setTotalBefore( userMoney );
        log.setTotal( userMoney.subtract( reduceMoney ) );
        int insertLogMoney = walletFundLogMapper.insert( log );
        if ( insertLogMoney <= 0 ) {
            throw new BusinessException( "资金日志记入失败,请重试" );
        }
    }
}
