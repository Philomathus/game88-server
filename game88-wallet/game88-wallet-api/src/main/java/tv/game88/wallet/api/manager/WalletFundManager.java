package tv.game88.wallet.api.manager;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.exception.NoMoneyException;
import tv.game88.common.utils.StringUtils;
import tv.game88.wallet.api.entity.WalletMerchantFundLog;
import tv.game88.wallet.api.entity.WalletUserFundLog;
import tv.game88.wallet.api.mapper.WalletMerchantFundLogMapper;
import tv.game88.wallet.api.mapper.WalletMerchantMapper;
import tv.game88.wallet.api.mapper.WalletUserFundLogMapper;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.type.WalletUserFundEnum;

import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Log4j2
public class WalletFundManager {
    @Resource
    private WalletUserFundLogMapper     walletUserFundLogMapper;
    @Resource
    private WalletMerchantFundLogMapper walletMerchantFundLogMapper;
    @Resource
    private WalletUserMapper            walletUserMapper;
    @Resource
    private WalletMerchantMapper        walletMerchantMapper;

    /**
     * 会员加钱
     *
     * @param userId   会员ID
     * @param addMoney 增加的部分
     * @param fundEnum 交易类型
     */
    @Transactional( rollbackFor = Exception.class )
    public void addWalletUserMoney( String userId, Long merchantId, Long addMoney, WalletUserFundEnum fundEnum, String mark,
                                    String businessId, String markorder ) {
        if ( fundEnum.getType() < 0 || ( !fundEnum.getIsTransaction() && merchantId == null ) ) {
            throw new BusinessException( "逻辑异常" );
        }
        Long userBalance = walletUserMapper.getUserMoney( userId );

        int updateMoney;
        if ( fundEnum.getIsTransaction() && fundEnum != WalletUserFundEnum.PERSONAL_TRANSFER_IN  && fundEnum != WalletUserFundEnum.CANCEL_ORDER_IN) {
            updateMoney = walletUserMapper.addChargeMoney( userId, addMoney );
        } else {
            updateMoney = walletUserMapper.addMoney( userId, addMoney );
        }

        //日志
        WalletUserFundLog log = new WalletUserFundLog();
        if ( StringUtils.isNotBlank( businessId ) ) {
            log.setId( businessId );
        } else {
            log.setId( IdWorker.get32UUID() );
        }
        log.setUserId( userId );
        log.setCreateTime( LocalDateTime.now() );
        log.setIncome( addMoney );
        log.setPay( 0L );
        log.setType( fundEnum.getType() );
        log.setDes( fundEnum.getDes() );
        log.setMark( mark );
        log.setTotalBefore( userBalance );
        log.setTotal( userBalance + addMoney );
        log.setMarkorder( markorder );
        int insertLogMoney = walletUserFundLogMapper.insert( log );
        if ( updateMoney <= 0 || insertLogMoney <= 0 ) {
            throw new BusinessException( "会员资金日志记入失败,请重试" );
        }
        // 用户加钱,商户减钱
        if ( !fundEnum.getIsTransaction() ) {
            this.reduceWalletMerchantMoney( merchantId, addMoney, fundEnum, mark, log.getId(), markorder, log.getCreateTime() );
        }
    }

    /**
     * 用户资金扣减
     *
     * @param userId      用户ID
     * @param reduceMoney 扣减金额
     * @param fundEnum    资金类型枚举 扣减CODE必须为负数
     * @param mark        备注
     */
    @Transactional( rollbackFor = Exception.class )
    public void reduceWalletUserMoney( String userId, Long merchantId, Long reduceMoney, WalletUserFundEnum fundEnum,
                                       String mark, String businessId, String markorder ) {
        if ( fundEnum.getType() > 0 || ( !fundEnum.getIsTransaction() && merchantId == null ) ) {
            throw new BusinessException( "逻辑异常" );
        }
        Long userMoney = walletUserMapper.getUserMoney( userId );
        //扣减金额
        int reducedMoney;
        if ( fundEnum.getIsTransaction() && fundEnum != WalletUserFundEnum.PERSONAL_TRANSFER_OUT ) {
            reducedMoney = walletUserMapper.reduceSaleMoney( userId, reduceMoney );
        } else {
            reducedMoney = walletUserMapper.reduceMoney( userId, reduceMoney );
        }
        if ( reducedMoney <= 0 ) {
            throw new NoMoneyException( "余额不足" );
        }
        //插入会员资金信息记录
        WalletUserFundLog log = new WalletUserFundLog();
        if ( StringUtils.isNotBlank( businessId ) ) {
            log.setId( businessId );
        } else {
            log.setId( IdWorker.get32UUID() );
        }
        log.setUserId( userId );
        log.setCreateTime( LocalDateTime.now() );
        log.setIncome( 0L );
        log.setPay( reduceMoney );
        log.setType( fundEnum.getType() );
        log.setDes( fundEnum.getDes() );
        log.setMark( mark );
        log.setTotalBefore( userMoney );
        log.setTotal( userMoney - reduceMoney );
        log.setMarkorder( markorder );
        int insertLogMoney = walletUserFundLogMapper.insert( log );
        if ( insertLogMoney <= 0 ) {
            throw new BusinessException( "会员资金日志记入失败,请重试" );
        }
        // 用户减钱,商户加钱
        if ( !fundEnum.getIsTransaction() ) {
            this.addWalletMerchantMoney( merchantId, reduceMoney, fundEnum, mark, log.getId(), markorder, log.getCreateTime() );
        }
    }

    /**
     * 商户加钱
     *
     * @param merchantId 商户ID
     * @param addMoney   增加的部分
     * @param fundEnum   交易类型
     */
    @Transactional( rollbackFor = Exception.class )
    public void addWalletMerchantMoney( Long merchantId, Long addMoney, WalletUserFundEnum fundEnum, String mark,
                                        String businessId, String markorder, LocalDateTime createTime ) {
        BigDecimal merchantBalance = walletMerchantMapper.getMerchantMoney( merchantId );

        int updateMoney = walletMerchantMapper.addMoney( merchantId, addMoney );

        //日志
        WalletMerchantFundLog merchantFundLog = new WalletMerchantFundLog();
        merchantFundLog.setId( businessId );
        merchantFundLog.setMerchantId( merchantId );
        merchantFundLog.setCreateTime( createTime );
        merchantFundLog.setIncome( new BigDecimal( addMoney ) );
        merchantFundLog.setPay( BigDecimal.ZERO );
        merchantFundLog.setType( fundEnum.getType() );
        merchantFundLog.setDes( fundEnum.getDes() );
        merchantFundLog.setMark( mark );
        merchantFundLog.setTotalBefore( merchantBalance );
        merchantFundLog.setTotal( merchantBalance.add( new BigDecimal( addMoney ) ) );
        merchantFundLog.setMarkorder( markorder );
        int insertMerchantLog = walletMerchantFundLogMapper.insert( merchantFundLog );
        if ( updateMoney <= 0 || insertMerchantLog <= 0 ) {
            throw new BusinessException( "商户资金日志记入失败,请重试" );
        }
    }

    /**
     * 商户资金扣减
     *
     * @param merchantId  商户ID
     * @param reduceMoney 扣减金额
     * @param fundEnum    交易类型
     */
    @Transactional( rollbackFor = Exception.class )
    public void reduceWalletMerchantMoney( Long merchantId, Long reduceMoney, WalletUserFundEnum fundEnum, String mark,
                                           String businessId, String markorder, LocalDateTime createTime ) {
        //扣减金额
        int reducedMoney = walletMerchantMapper.reduceMoney( merchantId, reduceMoney );

        if ( reducedMoney <= 0 ) {
            throw new NoMoneyException( "商户余额不足" );
        }

        BigDecimal merchantBalance = walletMerchantMapper.getMerchantMoney( merchantId );

        //日志
        WalletMerchantFundLog merchantFundLog = new WalletMerchantFundLog();
        merchantFundLog.setId( businessId );
        merchantFundLog.setMerchantId( merchantId );
        merchantFundLog.setCreateTime( createTime );
        merchantFundLog.setIncome( BigDecimal.ZERO );
        merchantFundLog.setPay( new BigDecimal( reduceMoney ) );
        merchantFundLog.setType( fundEnum.getType() );
        merchantFundLog.setDes( fundEnum.getDes() );
        merchantFundLog.setMark( mark );
        merchantFundLog.setTotalBefore( merchantBalance );
        merchantFundLog.setTotal( merchantBalance.subtract( new BigDecimal( reduceMoney ) ) );
        merchantFundLog.setMarkorder( markorder );
        int insertMerchantLog = walletMerchantFundLogMapper.insert( merchantFundLog );
        if ( insertMerchantLog <= 0 ) {
            throw new BusinessException( "商户资金日志记入失败,请重试" );
        }
    }
}
