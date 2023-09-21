package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.wallet.api.dto.ReqBuyCoins;
import tv.game88.wallet.api.dto.RspBuyOrderDetail;
import tv.game88.wallet.api.dto.RspPayMethod2;
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
import java.util.Arrays;
import java.util.List;

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
        WalletUserPayMethod bPayMethod = walletUserPayMethodMapper.selectById( reqBuyCoins.getPayMethodId() );
        if ( bPayMethod == null ) {
            return RspBase.businessError( "您的支付方式不存在" );
        }
        if ( bPayMethod.getAuditStatus() != null ) {
            return RspBase.businessError( "您的支付方式未审核或审核不通过,请选择其它支付方式" );
        }
        if ( !walletTransaction.getPayMethodTypes().contains( bPayMethod.getMethodType().name() ) ) {
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
        walletTransactionDetail.setBuyerPayMethodId( reqBuyCoins.getPayMethodId() );

        List<WalletUserPayMethod> sPayMethods = walletUserPayMethodMapper.selectBatchIds( Arrays.asList( walletTransaction
                .getPayMethodIds().split( "," ) ) );
        for ( WalletUserPayMethod sPayMethod : sPayMethods ) {
            if ( sPayMethod.getMethodType() == bPayMethod.getMethodType() ) {
                walletTransactionDetail.setSellerPayMethodId( sPayMethod.getMethodId() );
            }
        }
        if ( walletTransactionDetail.getSellerPayMethodId() == null ) {
            return RspBase.businessError( "您还未绑定该支付方式的支付账号" );
        }

        walletTransactionDetail.setRemark(
                "买家" + userId + "确认购买" + reqBuyCoins.getAmount() + ",时间:" + LocalDateTimeUtils.format( now ) );

        SpringUtils.getBean( WalletTransactionDetailService.class ).saveTransDetailOrReduceTransAmount( walletTransactionDetail );
        return RspBase.ok( "确认购买成功", walletTransactionDetail.getTransDetailId() );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveTransDetailOrReduceTransAmount( WalletTransactionDetail walletTransactionDetail ) {
        // 扣除挂单表金额并修改订单状态
        boolean update = walletTransactionService.update( new UpdateWrapper<WalletTransaction>()
                .setSql( "amount = amount - {0}", walletTransactionDetail.getAmount() ).set( "status", 1 )
                .eq( "transaction_id", walletTransactionDetail.getTransactionId() ).le( "status", 1 )
                .ge( "amount - " + walletTransactionDetail.getAmount(), 0 ) );
        // 保存交易
        int i = this.baseMapper.insert( walletTransactionDetail );
        if ( !( update && i > 0 ) ) {
            throw new BusinessException( "购买失败,请重试" );
        }
    }

    @Override
    public RspBase<RspBuyOrderDetail> buyOrderDetail( String userId, String transDetailId ) {
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return RspBase.businessError( "交易订单不存在" );
        }
        // 买家支付方式
        WalletUserPayMethod bPayMethod = walletUserPayMethodMapper.selectById( walletTransactionDetail.getBuyerPayMethodId() );
        if ( bPayMethod == null ) {
            return RspBase.businessError( "买家付款信息不存在" );
        }
        // 卖家支付方式
        WalletUserPayMethod sPayMethod = walletUserPayMethodMapper.selectById( walletTransactionDetail.getSellerPayMethodId() );
        if ( sPayMethod == null ) {
            return RspBase.businessError( "卖家收款信息不存在" );
        }

        RspBuyOrderDetail rspBuyOrderDetail = new RspBuyOrderDetail();
        rspBuyOrderDetail.setTransDetailId( transDetailId );
        rspBuyOrderDetail.setStatus( walletTransactionDetail.getStatus() );
        rspBuyOrderDetail.setAmount( walletTransactionDetail.getAmount() );
        rspBuyOrderDetail.setTransCertPic( walletTransactionDetail.getTransCertPic() );
        rspBuyOrderDetail.setPayMethodType( bPayMethod.getMethodType() );

        RspPayMethod2 rspBuyerPayMethod = new RspPayMethod2();
        BeanUtils.copyProperties( bPayMethod, rspBuyerPayMethod );
        rspBuyOrderDetail.setBuyerPayMethod( rspBuyerPayMethod );

        RspPayMethod2 rspSellerPayMethod = new RspPayMethod2();
        BeanUtils.copyProperties( sPayMethod, rspSellerPayMethod );
        rspBuyOrderDetail.setBuyerPayMethod( rspSellerPayMethod );

        // TODO 订单倒计时
        return RspBase.ok( rspBuyOrderDetail );
    }
}




