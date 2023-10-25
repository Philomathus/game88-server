package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletTransaction;
import tv.game88.wallet.api.entity.WalletTransactionDetail;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.entity.WalletUserPayMethod;
import tv.game88.wallet.api.manager.WalletFundManager;
import tv.game88.wallet.api.mapper.WalletTransactionDetailMapper;
import tv.game88.wallet.api.mapper.WalletUserPayMethodMapper;
import tv.game88.wallet.api.service.WalletMessageService;
import tv.game88.wallet.api.service.WalletTransactionDetailService;
import tv.game88.wallet.api.service.WalletTransactionService;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.api.type.EnumTransDetail;
import tv.game88.wallet.api.type.WalletTransEnum;
import tv.game88.wallet.api.type.WalletUserFundEnum;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    @Resource
    private WalletMessageService      walletMessageService;

    @Resource
    private WalletFundManager walletFundManager;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public List<RspTransDetail> transDetailList( String userId, ReqTransDetailList req ) {
        WalletTransactionDetail query = new WalletTransactionDetail();
        if ( req.getType() != null ) {
            if ( req.getType() == EnumTransDetail.buy ) {
                query.setBuyerId( userId );
            } else {
                query.setSellerId( userId );
            }
        } else {
            query.setBuySellId( userId );
        }
        if ( !CollectionUtils.isEmpty( req.getTransType() ) ) {
            query.setStatusList( req.getTransType() );
        }
        List<WalletTransactionDetail> walletTransactionDetails = this.baseMapper.selectWalletTransactionDetailList( query );
        List<RspTransDetail>          resultList               = new ArrayList<>();
        for ( WalletTransactionDetail walletTransactionDetail : walletTransactionDetails ) {
            RspTransDetail rspTransDetail = new RspTransDetail();
            rspTransDetail.setTransDetailId( walletTransactionDetail.getTransDetailId() );
            rspTransDetail.setAmount( walletTransactionDetail.getAmount() );
            rspTransDetail.setStatus( walletTransactionDetail.getStatus() );
            rspTransDetail.setTime( walletTransactionDetail.getTime() );
            rspTransDetail.setType( userId.equals( walletTransactionDetail.getSellerId() ) ? EnumTransDetail.sell :
                    EnumTransDetail.buy );
            resultList.add( rspTransDetail );
        }
        return resultList;
    }

    @Override
    public RspBase<?> buyOrder( String userId, ReqBuyCoins reqBuyCoins ) {
        // 买家用户
        WalletUser buyer   = walletUserService.getById( userId );
        RspBase    rspBase = walletUserService.validWalletUser( buyer );
        if ( rspBase != null ) {
            return rspBase;
        }
        String            transactionId     = reqBuyCoins.getTransactionId();
        WalletTransaction walletTransaction = walletTransactionService.getById( transactionId );
        rspBase = walletTransactionService.validTransaction( walletTransaction );
        if ( rspBase != null ) {
            return rspBase;
        }
        // 买家支付方式
        WalletUserPayMethod bPayMethod = walletUserPayMethodMapper.selectById( reqBuyCoins.getPayMethodId() );
        if ( bPayMethod == null ) {
            return RspBase.businessError( "您的支付方式不存在" );
        }
        if ( bPayMethod.getAuditStatus() != 1 ) {
            return RspBase.businessError( "您的支付方式未审核或审核不通过,请选择其它支付方式" );
        }
        if ( !walletTransaction.getPayMethodTypes().contains( bPayMethod.getMethodType().name() ) ) {
            return RspBase.businessError( "此挂单不支持您的支付方式" );
        }
        if ( reqBuyCoins.getAmount() > walletTransaction.getAmount() ) {
            return RspBase.businessError( "挂单余额不足" );
        }

        String          sellerId        = walletTransaction.getUserId();
        WalletTransEnum walletTransEnum = WalletTransEnum.BUYER_CONFIRM_BUY;
        String          transDetailId   = GenerateOrderCacheUtils.me.getOrderId( "BUY", 5 );

        WalletTransactionDetail walletTransactionDetail = new WalletTransactionDetail();
        walletTransactionDetail.setTransDetailId( transDetailId );
        walletTransactionDetail.setTransactionId( transactionId );
        walletTransactionDetail.setAmount( reqBuyCoins.getAmount() );
        walletTransactionDetail.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        walletTransactionDetail.setBuyerConfirmBuyTime( now );
        walletTransactionDetail.setTime( now );
        walletTransactionDetail.setSellerId( sellerId );
        walletTransactionDetail.setBuyerId( userId );
        walletTransactionDetail.setBuyerPayMethodId( reqBuyCoins.getPayMethodId() );

        List<WalletUserPayMethod> sPayMethods = walletUserPayMethodMapper.selectBatchIds( Arrays.asList( walletTransaction
                .getPayMethodIds()
                .split( "," ) ) );
        for ( WalletUserPayMethod sPayMethod : sPayMethods ) {
            if ( sPayMethod.getMethodType() == bPayMethod.getMethodType() ) {
                walletTransactionDetail.setSellerPayMethodId( sPayMethod.getMethodId() );
            }
        }
        if ( walletTransactionDetail.getSellerPayMethodId() == null ) {
            return RspBase.businessError( "您还未绑定该支付方式的支付账号" );
        }

        String remark = "买家" + userId + "确认购买" + reqBuyCoins.getAmount() + ",时间:" + LocalDateTimeUtils.format( now );
        walletTransactionDetail.setRemark( remark );

        SpringUtils.getBean( WalletTransactionDetailService.class ).saveTransDetailOrReduceTransAmount( walletTransactionDetail );

        // 卖家订单倒计时 5分钟后取消订单
        redisUtils.strSet( ConstantsWallet.BUYER_CONFIRM_BUY_ORDER
                + transDetailId, ConstantsWallet.REDIS_DEFAULT_VALUE, Duration.ofMinutes( 5 ) );

        // 通知消息给卖家
        walletMessageService.saveWalletMessage( sellerId, transactionId, walletTransEnum, true );

        return RspBase.ok( "确认购买成功", transDetailId );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveTransDetailOrReduceTransAmount( WalletTransactionDetail walletTransactionDetail ) {
        // 扣除挂单表金额并修改订单状态
        boolean update = walletTransactionService.update( new UpdateWrapper<WalletTransaction>()
                .setSql( "amount = amount - {0}", walletTransactionDetail.getAmount() )
                .set( "status", 1 )
                .eq( "transaction_id", walletTransactionDetail.getTransactionId() )
                .le( "status", 1 )
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
        rspBuyOrderDetail.setBuyerId( walletTransactionDetail.getBuyerId() );
        rspBuyOrderDetail.setSellerId( walletTransactionDetail.getSellerId() );

        RspPayMethod2 rspBuyerPayMethod = new RspPayMethod2();
        BeanUtils.copyProperties( bPayMethod, rspBuyerPayMethod );
        rspBuyOrderDetail.setBuyerPayMethod( rspBuyerPayMethod );

        RspPayMethod2 rspSellerPayMethod = new RspPayMethod2();
        BeanUtils.copyProperties( sPayMethod, rspSellerPayMethod );
        rspBuyOrderDetail.setBuyerPayMethod( rspSellerPayMethod );

        // 计算订单倒计时
        if ( redisUtils.exists( ConstantsWallet.BUYER_CONFIRM_BUY_ORDER + transDetailId ) ) {
            rspBuyOrderDetail.setCountdownSec( redisUtils.getExpire( ConstantsWallet.BUYER_CONFIRM_BUY_ORDER + transDetailId ) );
        }
        if ( redisUtils.exists( ConstantsWallet.SELLER_CONFIRM_TRANS_ORDER + transDetailId ) ) {
            rspBuyOrderDetail.setCountdownSec( redisUtils.getExpire(
                    ConstantsWallet.SELLER_CONFIRM_TRANS_ORDER + transDetailId ) );
        }
        if ( redisUtils.exists( ConstantsWallet.BUYER_CONFIRM_TRANSFER_ORDER + transDetailId ) ) {
            rspBuyOrderDetail.setCountdownSec( redisUtils.getExpire(
                    ConstantsWallet.BUYER_CONFIRM_TRANSFER_ORDER + transDetailId ) );
        }

        if ( userId.equals( walletTransactionDetail.getSellerId() )
                && walletTransactionDetail.getStatus() == WalletTransEnum.BUYER_CONFIRM_BUY ) {
            LocalDateTime now       = LocalDateTime.now();
            LocalDateTime startTime = now.minusDays( 30 );

            Long sellerTotalCount = this.baseMapper.countSellerTotal( walletTransactionDetail.getBuyerId(), startTime, now );

            Map<String, Object> sellerMap = this.baseMapper.sumSellerReceived( walletTransactionDetail.getBuyerId(), startTime,
                    now );
            Map<String, Object> buyerMap = this.baseMapper.sumBuyerTransfer( walletTransactionDetail.getBuyerId(), startTime,
                    now );

            int receivedTimeTotal  = Integer.parseInt( sellerMap.getOrDefault( "s", "0" ).toString() );
            int sellerSuccessCount = Integer.parseInt( sellerMap.getOrDefault( "c", "0" ).toString() );
            int transferTimeTotal  = Integer.parseInt( buyerMap.getOrDefault( "s", "0" ).toString() );
            int buyerSuccessCount  = Integer.parseInt( buyerMap.getOrDefault( "c", "0" ).toString() );

            String successRateMonth = sellerTotalCount == 0 ? "0.0%" : new BigDecimal( sellerSuccessCount )
                    .divide( new BigDecimal( sellerTotalCount ), 2, RoundingMode.HALF_UP )
                    .multiply( new BigDecimal( 100 ) )
                    .toString()
                    .concat( "%" );

            long aveReceivedTime = sellerSuccessCount == 0 ? 0 : receivedTimeTotal / sellerSuccessCount;
            long aveTransferTime = buyerSuccessCount == 0 ? 0 : transferTimeTotal / buyerSuccessCount;

            RspCreditInfo creditInfo = new RspCreditInfo();

            creditInfo.setSuccessNumMonth( sellerSuccessCount );
            creditInfo.setSuccessRateMonth( successRateMonth );
            creditInfo.setReceivedTimeMonth( LocalDateTimeUtils.secondsToTime( aveReceivedTime ) );
            creditInfo.setTransferTimeMonth( LocalDateTimeUtils.secondsToTime( aveTransferTime ) );

            WalletUser seller = walletUserService.getById( walletTransactionDetail.getBuyerId() );
            creditInfo.setBuyOrderNum( seller.getBuyOrderNum() );
            creditInfo.setSellOrderNum( seller.getSellOrderNum() );
            rspBuyOrderDetail.setCreditInfo( creditInfo );
        }

        return RspBase.ok( rspBuyOrderDetail );
    }

    /**
     * 卖家确认交易
     *
     * @param userId        卖家ID
     * @param transDetailId 交易ID
     */
    @Override
    public RspBase<?> sellerConfirmTrans( String userId, String transDetailId ) {
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return RspBase.businessError( "交易订单不存在" );
        }
        if ( !userId.equals( walletTransactionDetail.getSellerId() ) ) {
            return RspBase.businessError( "此卖单并不属于您" );
        }
        if ( walletTransactionDetail.getStatus() != WalletTransEnum.BUYER_CONFIRM_BUY ) {
            return RspBase.businessError( "买单状态有误,无法确认交易,请刷新订单后重试" );
        }
        WalletTransEnum         walletTransEnum = WalletTransEnum.SELLER_CONFIRM_TRANS;
        WalletTransactionDetail update          = new WalletTransactionDetail();
        update.setTransDetailId( transDetailId );
        update.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        update.setSellerConfirmTransTime( now );
        update.setTime( now );
        String remark = "\n卖家" + userId + "确认交易,时间:" + LocalDateTimeUtils.format( now );
        update.setRemark( walletTransactionDetail.getRemark().concat( remark ) );
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            // 取消超时订单
            redisUtils.unlink( ConstantsWallet.BUYER_CONFIRM_BUY_ORDER + transDetailId );

            // 买家订单倒计时 20分钟 用于转账
            redisUtils.strSet( ConstantsWallet.SELLER_CONFIRM_TRANS_ORDER
                    + transDetailId, ConstantsWallet.REDIS_DEFAULT_VALUE, Duration.ofMinutes( 20 ) );

            // 消息通知买家
            walletMessageService.saveWalletMessage( walletTransactionDetail.getBuyerId(), transDetailId, walletTransEnum, false );

            return RspBase.ok( "确认交易成功" );
        }
        return RspBase.businessError( "确认交易失败,请重试" );
    }

    /**
     * 卖家取消交易
     *
     * @param userId        卖家ID
     * @param transDetailId 交易ID
     */
    @Override
    public RspBase<?> sellerCancelTrans( String userId, String transDetailId ) {
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return RspBase.businessError( "交易订单不存在" );
        }
        if ( !userId.equals( walletTransactionDetail.getSellerId() ) ) {
            return RspBase.businessError( "此卖单并不属于您" );
        }
        if ( walletTransactionDetail.getStatus() != WalletTransEnum.BUYER_CONFIRM_BUY ) {
            return RspBase.businessError( "买单状态有误,无法确认交易,请刷新订单后重试" );
        }
        WalletTransEnum walletTransEnum = WalletTransEnum.SELLER_CANCEL;

        WalletTransactionDetail update = new WalletTransactionDetail();
        update.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        update.setCancelTime( now );
        update.setTime( now );
        String remark = "\n卖家" + userId + "取消交易,时间:" + LocalDateTimeUtils.format( now );
        update.setRemark( walletTransactionDetail.getRemark().concat( remark ) );

        SpringUtils
                .getBean( WalletTransactionDetailService.class )
                .updateTransDetailOrAddTransAmount( update, walletTransactionDetail );

        // 消息通知买家
        walletMessageService.saveWalletMessage( walletTransactionDetail.getBuyerId(), transDetailId, walletTransEnum, false );

        return RspBase.ok( "确认取消交易成功", transDetailId );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateTransDetailOrAddTransAmount( WalletTransactionDetail updateTransactionDetail,
                                                   WalletTransactionDetail walletTransactionDetail ) {
        // 保存状态
        int i = this.baseMapper.update( updateTransactionDetail, new LambdaUpdateWrapper<WalletTransactionDetail>()
                .in( WalletTransactionDetail::getStatus, WalletTransEnum.BUYER_CONFIRM_BUY, WalletTransEnum.SELLER_CONFIRM_TRANS )
                .eq( WalletTransactionDetail::getTransDetailId, walletTransactionDetail.getTransDetailId() ) );
        // 回退挂单金额
        boolean update = walletTransactionService.update( new UpdateWrapper<WalletTransaction>()
                .setSql( "amount = amount + {0}", walletTransactionDetail.getAmount() )
                .eq( "transaction_id", walletTransactionDetail.getTransactionId() )
                .eq( "status", 1 ) );
        if ( update && i > 0 ) {
            // 确认是否存在其它未完成的订单
            boolean exists = this.baseMapper.exists( new LambdaQueryWrapper<WalletTransactionDetail>()
                    .eq( WalletTransactionDetail::getSellerId, walletTransactionDetail.getSellerId() )
                    .in( WalletTransactionDetail::getStatus, WalletTransEnum.BUYER_CONFIRM_BUY,
                            WalletTransEnum.SELLER_CONFIRM_TRANS, WalletTransEnum.BUYER_CONFIRM_TRANSFER,
                            WalletTransEnum.SELLER_NOT_RECEIVED )
                    .ne( WalletTransactionDetail::getTransDetailId, walletTransactionDetail.getTransDetailId() ) );
            // 如果不存在则将挂单改为挂单中
            if ( !exists ) {
                boolean updateTrans = walletTransactionService.update( new LambdaUpdateWrapper<WalletTransaction>()
                        .set( WalletTransaction::getStatus, 0 )
                        .eq( WalletTransaction::getTransactionId, walletTransactionDetail.getTransactionId() ) );
                if ( !updateTrans ) {
                    throw new BusinessException( "取消交易失败,请重试" );
                }
            }
        } else {
            throw new BusinessException( "取消交易失败,请重试" );
        }
    }

    /**
     * 买家确认转账
     *
     * @param userId                  买家ID
     * @param reqBuyerConfirmTransfer 交易凭证和交易ID
     */
    @Override
    public RspBase<?> buyerConfirmTransfer( String userId, ReqBuyerConfirmTransfer reqBuyerConfirmTransfer ) {
        String                  transDetailId           = reqBuyerConfirmTransfer.getTransDetailId();
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return RspBase.businessError( "交易订单不存在" );
        }
        if ( !userId.equals( walletTransactionDetail.getBuyerId() ) ) {
            return RspBase.businessError( "此买单并不属于您" );
        }
        if ( walletTransactionDetail.getStatus() != WalletTransEnum.SELLER_CONFIRM_TRANS ) {
            return RspBase.businessError( "买单状态有误,无法确认转账,请刷新订单后重试" );
        }
        WalletTransEnum         walletTransEnum = WalletTransEnum.BUYER_CONFIRM_TRANSFER;
        WalletTransactionDetail update          = new WalletTransactionDetail();
        update.setTransDetailId( transDetailId );
        update.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        update.setBuyerConfirmTransferTime( now );
        update.setTime( now );
        update.setTransCertPic( reqBuyerConfirmTransfer.getTransCertPic() );
        String remark = "\n买家" + userId + "确认转账,时间:" + LocalDateTimeUtils.format( now );
        update.setRemark( walletTransactionDetail.getRemark().concat( remark ) );
        long intervalTime = LocalDateTimeUtils.getIntervalTime( walletTransactionDetail.getSellerConfirmTransTime(), now );
        update.setTransferTimeSec( ( int ) ( intervalTime / 1000 ) );
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            // 取消超时订单
            redisUtils.unlink( ConstantsWallet.SELLER_CONFIRM_TRANS_ORDER + transDetailId );

            // 卖家 订单倒计时 30分钟 用于确认是否收到转账
            redisUtils.strSet( ConstantsWallet.BUYER_CONFIRM_TRANSFER_ORDER
                    + transDetailId, ConstantsWallet.REDIS_DEFAULT_VALUE, Duration.ofMinutes( 30 ) );

            // 消息通知卖家
            walletMessageService.saveWalletMessage( walletTransactionDetail.getSellerId(), transDetailId, walletTransEnum, true );

            return RspBase.ok( "确认转账成功" );
        }
        return RspBase.businessError( "确认转账失败,请重试" );
    }

    /**
     * 买家取消交易
     *
     * @param userId        买家ID
     * @param transDetailId 交易ID
     */
    @Override
    public RspBase<?> buyerCancelTrans( String userId, String transDetailId ) {
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return RspBase.businessError( "交易订单不存在" );
        }
        if ( !userId.equals( walletTransactionDetail.getBuyerId() ) ) {
            return RspBase.businessError( "此买单并不属于您" );
        }
        if ( walletTransactionDetail.getStatus() != WalletTransEnum.SELLER_CONFIRM_TRANS ) {
            return RspBase.businessError( "买单状态有误,无法取消交易,请刷新订单后重试" );
        }
        WalletTransEnum walletTransEnum = WalletTransEnum.BUYER_CANCEL;

        WalletTransactionDetail update = new WalletTransactionDetail();
        update.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        update.setCancelTime( now );
        update.setTime( now );
        String remark = "\n买家" + userId + "取消交易,时间:" + LocalDateTimeUtils.format( now );
        update.setRemark( walletTransactionDetail.getRemark().concat( remark ) );

        SpringUtils
                .getBean( WalletTransactionDetailService.class )
                .updateTransDetailOrAddTransAmount( update, walletTransactionDetail );

        // 取消超时订单
        redisUtils.unlink( ConstantsWallet.SELLER_CONFIRM_TRANS_ORDER + transDetailId );

        // 消息通知卖家
        walletMessageService.saveWalletMessage( walletTransactionDetail.getSellerId(), transDetailId, walletTransEnum, true );

        return RspBase.ok( "确认取消交易成功", walletTransactionDetail.getTransDetailId() );
    }

    /**
     * 卖家确认转币
     *
     * @param userId        卖家ID
     * @param transDetailId 交易ID
     */
    @Override
    public RspBase<?> sellerConfirmTransfer( String userId, String transDetailId ) {
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return RspBase.businessError( "交易订单不存在" );
        }
        if ( !userId.equals( walletTransactionDetail.getSellerId() ) ) {
            return RspBase.businessError( "此卖单并不属于您" );
        }
        if ( walletTransactionDetail.getStatus() != WalletTransEnum.BUYER_CONFIRM_TRANSFER ) {
            return RspBase.businessError( "买单状态有误,无法确认转币,请刷新订单后重试" );
        }
        WalletTransEnum walletTransEnum = WalletTransEnum.SELLER_CONFIRM_TRANSFER;

        // 修改订单状态并给买家加币
        WalletTransactionDetail update = new WalletTransactionDetail();
        update.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        update.setSuccessTransTime( now );
        update.setTime( now );
        String remark = "\n卖家" + userId + "确认转币,时间:" + LocalDateTimeUtils.format( now );
        update.setRemark( walletTransactionDetail.getRemark().concat( remark ) );
        long intervalTime = LocalDateTimeUtils.getIntervalTime( walletTransactionDetail.getBuyerConfirmTransferTime(), now );
        update.setReceivedTimeSec( ( int ) ( intervalTime / 1000 ) );

        SpringUtils
                .getBean( WalletTransactionDetailService.class )
                .updateTransDetailOrAddUserAmount( update, walletTransactionDetail );

        // 取消超时订单
        redisUtils.unlink( ConstantsWallet.BUYER_CONFIRM_TRANSFER_ORDER + transDetailId );

        // 通知消息给买家
        walletMessageService.saveWalletMessage( walletTransactionDetail.getBuyerId(), transDetailId, walletTransEnum, false );

        return RspBase.ok( "确认转币成功", transDetailId );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateTransDetailOrAddUserAmount( WalletTransactionDetail updateTransactionDetail,
                                                  WalletTransactionDetail walletTransactionDetail ) {
        // 保存状态
        int i = this.baseMapper.update( updateTransactionDetail, new LambdaUpdateWrapper<WalletTransactionDetail>()
                .eq( WalletTransactionDetail::getStatus, WalletTransEnum.BUYER_CONFIRM_TRANSFER )
                .eq( WalletTransactionDetail::getTransDetailId, walletTransactionDetail.getTransDetailId() ) );
        // 给买家加币
        WalletUserFundEnum fundEnum = WalletUserFundEnum.TRANSACTION_ORDER_IN;
        String             mark     = "用户" + fundEnum.getDes() + walletTransactionDetail.getAmount();
        walletFundManager.addWalletUserMoney( walletTransactionDetail.getBuyerId(), null, walletTransactionDetail.getAmount(),
                fundEnum, mark, walletTransactionDetail.getTransDetailId(), walletTransactionDetail.getTransDetailId() );
        if ( i > 0 ) {
            // 确认是否存在其它未完成的订单
            boolean exists = this.baseMapper.exists( new LambdaQueryWrapper<WalletTransactionDetail>()
                    .eq( WalletTransactionDetail::getSellerId, walletTransactionDetail.getSellerId() )
                    .in( WalletTransactionDetail::getStatus, WalletTransEnum.BUYER_CONFIRM_BUY,
                            WalletTransEnum.SELLER_CONFIRM_TRANS, WalletTransEnum.BUYER_CONFIRM_TRANSFER,
                            WalletTransEnum.SELLER_NOT_RECEIVED )
                    .ne( WalletTransactionDetail::getTransDetailId, walletTransactionDetail.getTransDetailId() ) );
            WalletTransaction walletTransaction = walletTransactionService.getOne( new LambdaQueryWrapper<WalletTransaction>()
                    .eq( WalletTransaction::getTransactionId, walletTransactionDetail.getTransactionId() )
                    .select( WalletTransaction::getAmount ) );
            // 如果不存在并且挂单金额为0则将挂单改为交易成功
            if ( !exists && walletTransaction != null && walletTransaction.getAmount() <= 0 ) {
                boolean updateTrans = walletTransactionService.update( new LambdaUpdateWrapper<WalletTransaction>()
                        .set( WalletTransaction::getStatus, 0 )
                        .eq( WalletTransaction::getTransactionId, walletTransactionDetail.getTransactionId() ) );
                if ( !updateTrans ) {
                    throw new BusinessException( "转币失败,请重试" );
                }
            }
        } else {
            throw new BusinessException( "转币失败,请重试" );
        }
    }

    /**
     * 卖家未收到转账
     *
     * @param userId        卖家ID
     * @param transDetailId 交易ID
     */
    @Override
    public RspBase<?> sellerNotReceived( String userId, String transDetailId ) {
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return RspBase.businessError( "交易订单不存在" );
        }
        if ( !userId.equals( walletTransactionDetail.getSellerId() ) ) {
            return RspBase.businessError( "此卖单并不属于您" );
        }
        if ( walletTransactionDetail.getStatus() != WalletTransEnum.BUYER_CONFIRM_TRANSFER ) {
            return RspBase.businessError( "买单状态有误,无法确认转币,请刷新订单后重试" );
        }
        WalletTransEnum walletTransEnum = WalletTransEnum.SELLER_NOT_RECEIVED;
        // 修改订单状态并让管理员处理
        WalletTransactionDetail update = new WalletTransactionDetail();
        update.setTransDetailId( transDetailId );
        update.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        update.setSellerNotReceivedTime( now );
        update.setTime( now );
        String remark = "\n卖家" + userId + "未收到转账,时间:" + LocalDateTimeUtils.format( now );
        update.setRemark( walletTransactionDetail.getRemark().concat( remark ) );
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            // 取消超时订单
            redisUtils.unlink( ConstantsWallet.BUYER_CONFIRM_TRANSFER_ORDER + transDetailId );

            // 消息通知买家
            walletMessageService.saveWalletMessage( walletTransactionDetail.getBuyerId(), transDetailId, walletTransEnum, false );

            return RspBase.ok( "确认未收到转账" );
        }
        return RspBase.businessError( "确认未收到转账失败,请重试" );
    }

    /**
     * 若状态未变,则卖家取消订单
     *
     * @param transDetailId 交易订单号
     */
    @Override
    public void processBuyerConfirmBuyTimeout( String transDetailId ) {
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return;
        }
        if ( walletTransactionDetail.getStatus() != WalletTransEnum.BUYER_CONFIRM_BUY ) {
            return;
        }
        WalletTransEnum walletTransEnum = WalletTransEnum.SELLER_CANCEL;

        WalletTransactionDetail update = new WalletTransactionDetail();
        update.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        update.setCancelTime( now );
        update.setTime( now );
        String remark =
                "\n卖家" + walletTransactionDetail.getSellerId() + "超时取消交易,时间:" + LocalDateTimeUtils.format( now );
        update.setRemark( walletTransactionDetail.getRemark().concat( remark ) );

        SpringUtils
                .getBean( WalletTransactionDetailService.class )
                .updateTransDetailOrAddTransAmount( update, walletTransactionDetail );

        // 消息通知卖家和买家
        walletMessageService.saveWalletMessage( walletTransactionDetail.getSellerId(), transDetailId, walletTransEnum, true );
        walletMessageService.saveWalletMessage( walletTransactionDetail.getBuyerId(), transDetailId, walletTransEnum, false );
    }

    /**
     * 若状态未变,则买家取消订单
     *
     * @param transDetailId 交易订单号
     */
    @Override
    public void processSellerConfirmTransTimeout( String transDetailId ) {
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return;
        }
        if ( walletTransactionDetail.getStatus() != WalletTransEnum.SELLER_CONFIRM_TRANS ) {
            return;
        }
        WalletTransEnum walletTransEnum = WalletTransEnum.BUYER_CANCEL;

        WalletTransactionDetail update = new WalletTransactionDetail();
        update.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        update.setCancelTime( now );
        update.setTime( now );
        String remark = "\n买家" + walletTransactionDetail.getBuyerId() + "超时取消交易,时间:" + LocalDateTimeUtils.format( now );
        update.setRemark( walletTransactionDetail.getRemark().concat( remark ) );

        SpringUtils
                .getBean( WalletTransactionDetailService.class )
                .updateTransDetailOrAddTransAmount( update, walletTransactionDetail );

        // 消息通知卖家和买家
        walletMessageService.saveWalletMessage( walletTransactionDetail.getSellerId(), transDetailId, walletTransEnum, true );
        walletMessageService.saveWalletMessage( walletTransactionDetail.getBuyerId(), transDetailId, walletTransEnum, false );
    }

    /**
     * 若状态未变,则卖家放币给买家
     *
     * @param transDetailId 交易订单号
     */
    @Override
    public void processBuyerConfirmTransferTimeout( String transDetailId ) {
        WalletTransactionDetail walletTransactionDetail = this.baseMapper.selectById( transDetailId );
        if ( walletTransactionDetail == null ) {
            return;
        }
        if ( walletTransactionDetail.getStatus() != WalletTransEnum.BUYER_CONFIRM_TRANSFER ) {
            return;
        }
        WalletTransEnum walletTransEnum = WalletTransEnum.SYSTEM_CONFIRM_TRANSFER;
        // 修改订单状态并给买家加币
        WalletTransactionDetail update = new WalletTransactionDetail();
        update.setStatus( walletTransEnum );
        LocalDateTime now = LocalDateTime.now();
        update.setSuccessTransTime( now );
        update.setTime( now );
        String remark = "\n卖家" + walletTransactionDetail.getSellerId() + "长时间未操作,系统确认转币,时间:"
                + LocalDateTimeUtils.format( now );
        update.setRemark( walletTransactionDetail.getRemark().concat( remark ) );

        SpringUtils
                .getBean( WalletTransactionDetailService.class )
                .updateTransDetailOrAddUserAmount( update, walletTransactionDetail );

        // 通知消息给买家和卖家
        walletMessageService.saveWalletMessage( walletTransactionDetail.getSellerId(), transDetailId, walletTransEnum, true );
        walletMessageService.saveWalletMessage( walletTransactionDetail.getBuyerId(), transDetailId, walletTransEnum, false );
    }
}




