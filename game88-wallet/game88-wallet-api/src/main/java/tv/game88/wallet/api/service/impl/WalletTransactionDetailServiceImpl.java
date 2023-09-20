package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.wallet.api.dto.ReqBuyCoins;
import tv.game88.wallet.api.entity.WalletTransaction;
import tv.game88.wallet.api.entity.WalletTransactionDetail;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.entity.WalletUserPayMethod;
import tv.game88.wallet.api.mapper.WalletTransactionDetailMapper;
import tv.game88.wallet.api.mapper.WalletUserPayMethodMapper;
import tv.game88.wallet.api.service.WalletTransactionDetailService;
import tv.game88.wallet.api.service.WalletTransactionService;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.api.type.WalletTransEnum;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author meng.jun
 * @description 针对表【wallet_transaction_detail(钱包交易明细表)】的数据库操作Service实现
 * @createDate 2023-08-21 17:31:44
 */
@Service
public class WalletTransactionDetailServiceImpl extends ServiceImpl<WalletTransactionDetailMapper, WalletTransactionDetail> implements WalletTransactionDetailService {
    @Resource
    private WalletUserService         walletUserService;
    @Resource
    private WalletTransactionService  walletTransactionService;
    @Resource
    private WalletUserPayMethodMapper walletUserPayMethodMapper;

    @Override
    public RspBase<?> buyOrder( String userId, ReqBuyCoins reqBuyCoins ) {
        // 买家用户
        WalletUser buyer   = walletUserService.getById( userId );
        RspBase    rspBase = walletUserService.validWalletUser( buyer );
        if ( rspBase != null ) {
            return rspBase;
        }
        WalletTransaction walletTransaction = walletTransactionService.getById( reqBuyCoins.getTransactionId() );
        rspBase = walletTransactionService.validTransaction( walletTransaction );
        if ( rspBase != null ) {
            return rspBase;
        }
        // 买家支付方式
        WalletUserPayMethod walletUserPayMethod = walletUserPayMethodMapper.selectById( reqBuyCoins.getPayMethodId() );
        if ( walletUserPayMethod == null ) {
            return RspBase.businessError( "您的支付方式不存在" );
        }
        if ( walletUserPayMethod.getAuditStatus() != null ) {
            return RspBase.businessError( "您的支付方式未审核或审核不通过,请选择其它支付方式" );
        }
        if ( !walletTransaction.getPayMethodTypes().contains( walletUserPayMethod.getMethodType().name() ) ) {
            return RspBase.businessError( "此挂单不支持您的支付方式" );
        }
        if ( reqBuyCoins.getAmount() > walletTransaction.getAmount() ) {
            return RspBase.businessError( "挂单余额不足" );
        }
        WalletTransactionDetail walletTransactionDetail = new WalletTransactionDetail();
        walletTransactionDetail.setTransDetailId( GenerateOrderCacheUtils.me.getOrderId( "BUY", 5 ) );
        walletTransactionDetail.setTransactionId( reqBuyCoins.getTransactionId() );
        walletTransactionDetail.setAmount( reqBuyCoins.getAmount() );
        walletTransactionDetail.setStatus( WalletTransEnum.BUYER_CONFIRM_BUY );
        LocalDateTime now = LocalDateTime.now();
        walletTransactionDetail.setBuyerConfirmBuyTime( now );
        walletTransactionDetail.setSellerId( walletTransaction.getUserId() );
        walletTransactionDetail.setBuyerId( userId );
        walletTransactionDetail.setRemark(
                "买家" + userId + "确认购买" + reqBuyCoins.getAmount() + ",时间:" + LocalDateTimeUtils.format( now ) );

        SpringUtils.getBean( WalletTransactionDetailService.class ).saveTransDetailOrReduceTransAmount( walletTransactionDetail );
        return RspBase.ok( "确认购买成功" );
    }

    @Override
    public void saveTransDetailOrReduceTransAmount( WalletTransactionDetail walletTransactionDetail ) {

    }
}




