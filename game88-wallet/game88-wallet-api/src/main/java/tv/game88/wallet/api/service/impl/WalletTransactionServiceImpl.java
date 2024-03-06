package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigBankListCache;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletTransaction;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.entity.WalletUserPayMethod;
import tv.game88.wallet.api.manager.WalletFundManager;
import tv.game88.wallet.api.mapper.WalletTransactionDetailMapper;
import tv.game88.wallet.api.mapper.WalletTransactionMapper;
import tv.game88.wallet.api.mapper.WalletUserPayMethodMapper;
import tv.game88.wallet.api.service.WalletTransactionService;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.api.type.WalletPayMethodEnum;
import tv.game88.wallet.api.type.WalletUserFundEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author meng.jun
 * @description 针对表【wallet_transaction(钱包交易表)】的数据库操作Service实现
 * @createDate 2023-08-21 17:27:31
 */
@Service
public class WalletTransactionServiceImpl extends ServiceImpl<WalletTransactionMapper, WalletTransaction> implements WalletTransactionService {
    @Resource
    private WalletUserService             walletUserService;
    @Resource
    private WalletUserPayMethodMapper     walletUserPayMethodMapper;
    @Resource
    private WalletTransactionDetailMapper walletTransactionDetailMapper;
    @Resource
    private WalletFundManager             walletFundManager;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private ConfigBankListCache configBankListCache;

    @Override
    public RspBase<String> sellOrder( String userId, ReqSellCoins reqSellCoins ) {
        if ( reqSellCoins.getCanSplit() && reqSellCoins.getMinBuyNum() == null ) {
            return RspBase.businessError( "最低购买数量不能为空" );
        }
        if ( !reqSellCoins.getCanSplit() ) {
            reqSellCoins.setMinBuyNum( null );
        }
        if ( reqSellCoins.getMinBuyNum() != null && reqSellCoins.getMinBuyNum() >= reqSellCoins.getSellNum() ) {
            return RspBase.businessError( "最低出售数量不能超过或等于出售数量" );
        }
        WalletUser walletUser = walletUserService.getById( userId );
        RspBase    rspBase    = walletUserService.validWalletUser( walletUser );
        if ( rspBase != null ) {
            return rspBase;
        }
        if ( walletUser.getAmount() < reqSellCoins.getSellNum() ) {
            return RspBase.businessError( "您的G币不足,G币数量:" + walletUser.getAmount() );
        }

        if ( !passwordEncoder.matches( reqSellCoins.getFundPass(), walletUser.getFundPassword() ) ) {
            return RspBase.businessError( "密码不匹配" );
        }

        Set<String> typeSet = walletUserPayMethodMapper
                .selectBatchIds( reqSellCoins.getPayMethodIds() )
                .stream()
                .map( pm -> pm.getMethodType().name() )
                .collect( Collectors.toSet() );

        LocalDateTime now     = LocalDateTime.now();
        Long          sellNum = reqSellCoins.getSellNum();

        WalletTransaction walletTransaction = new WalletTransaction();
        walletTransaction.setTransactionId( GenerateOrderCacheUtils.me.getOrderId( "JY", 6 ) );
        walletTransaction.setUserId( userId );
        walletTransaction.setStatus( 0 );
        walletTransaction.setCanSplit( reqSellCoins.getCanSplit() );
        walletTransaction.setMinBuyNum( reqSellCoins.getMinBuyNum() );
        walletTransaction.setAmount( sellNum );
        walletTransaction.setCreateTime( now );
        walletTransaction.setPayMethodIds( StringUtils.join( reqSellCoins.getPayMethodIds(), "," ) );
        walletTransaction.setPayMethodTypes( StringUtils.join( typeSet, "," ) );

        LocalDateTime startTime = now.minusDays( 30 );

        Long                sellerTotalCount = walletTransactionDetailMapper.countSellerTotal( userId, startTime, now );
        Map<String, Object> sellerMap        = walletTransactionDetailMapper.sumSellerReceived( userId, startTime, now );
        Map<String, Object> buyerMap         = walletTransactionDetailMapper.sumBuyerTransfer( userId, startTime, now );

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

        walletTransaction.setSuccessNumMonth( sellerSuccessCount );
        walletTransaction.setSuccessRateMonth( successRateMonth );
        walletTransaction.setReceivedTimeMonth( LocalDateTimeUtils.secondsToTime( aveReceivedTime ) );
        walletTransaction.setTransferTimeMonth( LocalDateTimeUtils.secondsToTime( aveTransferTime ) );

        SpringUtils.getBean( WalletTransactionService.class ).saveTransAndReduceUserAmount( userId, walletTransaction, sellNum );
        return RspBase.ok( "挂单成功", walletTransaction.getTransactionId() );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveTransAndReduceUserAmount( String userId, WalletTransaction walletTransaction, Long sellNum ) {
        int i = this.baseMapper.insert( walletTransaction );
        if ( i > 0 ) {
            // 扣除会员金额
            WalletUserFundEnum fundEnum = WalletUserFundEnum.PUT_ORDER_OUT;
            String             mark     = "用户" + fundEnum.getDes() + sellNum;
            walletFundManager.reduceWalletUserMoney( userId, null, sellNum, fundEnum, mark,
                    walletTransaction.getTransactionId(), walletTransaction.getTransactionId() );
            walletUserService.addSellerTotalSellingAmount( userId, walletTransaction.getAmount() );
        } else {
            throw new BusinessException( "发布挂单失败,请重试" );
        }
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void updateTransAndAddUserAmount( String userId, WalletTransaction update, Long amount ) {
        int i = this.baseMapper.updateById( update );
        if ( i > 0 ) {
            // 扣除会员金额
            WalletUserFundEnum fundEnum = WalletUserFundEnum.CANCEL_ORDER_IN;
            String             mark     = "用户" + fundEnum.getDes() + amount;
            walletFundManager.addWalletUserMoney( userId, null, amount, fundEnum, mark,
                    update.getTransactionId() + "QuXiao", update.getTransactionId() );
            walletUserService.addSellerInitCancelSell( userId, amount );
        } else {
            throw new BusinessException( "取消挂单失败,请重试" );
        }
    }

    @Override
    public RspBase<RspSellOrderDetail> sellOrderDetail( String userId, String transactionId ) {
        WalletUser walletUser = walletUserService.getById( userId );
        RspBase    rspBase    = walletUserService.validWalletUser( walletUser );
        if ( rspBase != null ) {
            return rspBase;
        }
        WalletTransaction walletTransaction = this.baseMapper.selectById( transactionId );
        if ( walletTransaction == null ) {
            return RspBase.businessError( "此挂单不存在" );
        }
        if ( !walletTransaction.getUserId().equals( userId ) ) {
            return RspBase.businessError( "此挂单并不属于您" );
        }
        RspSellOrderDetail rspSellOrderDetail = new RspSellOrderDetail();
        BeanUtils.copyProperties( walletTransaction, rspSellOrderDetail );

        String[]                  payMethodIds   = walletTransaction.getPayMethodIds().split( "," );
        List<WalletUserPayMethod> userPayMethods = walletUserPayMethodMapper.selectBatchIds( Arrays.asList( payMethodIds ) );

        List<RspConfigBankList> configBankList = configBankListCache.getEffectList();

        Map<String, RspPayMethod2> rspPayMethodMap = new HashMap<>();
        for ( WalletUserPayMethod userPayMethod : userPayMethods ) {
            RspPayMethod2 rspPayMethod = new RspPayMethod2();
            BeanUtils.copyProperties( userPayMethod, rspPayMethod );
            rspPayMethodMap.put( userPayMethod.getMethodType().name(), rspPayMethod );

            for ( RspConfigBankList rspConfigBank : configBankList ) {
                if ( Objects.equals( userPayMethod.getBankId(), rspConfigBank.getId() ) ) {
                    rspPayMethod.setBankName( rspConfigBank.getBankName() );
                }
            }
        }
        rspSellOrderDetail.setRspPayMethodMap( rspPayMethodMap );
        return RspBase.ok( rspSellOrderDetail );
    }

    @Override
    public RspBase<?> cancelSellOrder( String userId, String transactionId ) {
        WalletUser walletUser = walletUserService.getById( userId );
        RspBase    rspBase    = walletUserService.validWalletUser( walletUser );
        if ( rspBase != null ) {
            return rspBase;
        }
        WalletTransaction walletTransaction = this.baseMapper.selectById( transactionId );
        if ( walletTransaction == null ) {
            return RspBase.businessError( "此挂单不存在" );
        }
        if ( !walletTransaction.getUserId().equals( userId ) ) {
            return RspBase.businessError( "此挂单并不属于您" );
        }
        if ( walletTransaction.getStatus() == 1 ) {
            return RspBase.businessError( "挂单正在交易中" );
        }
        if ( walletTransaction.getStatus() == 2 ) {
            return RspBase.businessError( "挂单交易已成功" );
        }
        if ( walletTransaction.getStatus() == 3 ) {
            return RspBase.businessError( "挂单交易已取消" );
        }
        WalletTransaction update = new WalletTransaction();
        update.setTransactionId( transactionId );
        update.setStatus( 3 );
        update.setTransEndTime( LocalDateTime.now() );

        SpringUtils
                .getBean( WalletTransactionService.class )
                .updateTransAndAddUserAmount( userId, update, walletTransaction.getAmount() );
        return RspBase.ok( "挂单取消成功" );
    }

    @Override
    public List<RspSellOrderDetail> sellOrderList( String userId, ReqSellOrderList reqSellOrderList ) {
        reqSellOrderList.setUserId( userId );
        WalletTransaction query = new WalletTransaction();
        BeanUtils.copyProperties( reqSellOrderList, query );
        if ( CollectionUtils.isEmpty( reqSellOrderList.getPayMethodType() ) ) {
            reqSellOrderList.setPayMethodType( List.of( WalletPayMethodEnum.values() ) );
        }
        query.setPayMethodTypeList( reqSellOrderList.getPayMethodType() );

        List<WalletTransaction>  walletTransactions = this.baseMapper.selectWalletTransactionList( query );
        List<RspSellOrderDetail> resultList         = new ArrayList<>();
        for ( WalletTransaction transaction : walletTransactions ) {
            RspSellOrderDetail rspSellOrderDetail = new RspSellOrderDetail();
            BeanUtils.copyProperties( transaction, rspSellOrderDetail );

            resultList.add( rspSellOrderDetail );
        }
        return resultList;
    }

    @Override
    public List<RspTransCenterDetail> transSellOrderList( String userId, ReqTransCenterDetail reqTransCenterDetail ) {
        WalletTransaction query = new WalletTransaction();
        BeanUtils.copyProperties( reqTransCenterDetail, query );
        if ( CollectionUtils.isEmpty( reqTransCenterDetail.getPayMethodType() ) ) {
            reqTransCenterDetail.setPayMethodType( List.of( WalletPayMethodEnum.values() ) );
        }
        query.setPayMethodTypeList( reqTransCenterDetail.getPayMethodType() );
        query.setStatusList( Arrays.asList( 0, 1 ) );
        query.setUnUserId( userId );

        List<WalletTransaction>    walletTransactions = this.baseMapper.selectWalletTransactionList( query );
        List<RspTransCenterDetail> resultList         = new ArrayList<>();
        for ( WalletTransaction transaction : walletTransactions ) {
            RspTransCenterDetail rspSellOrderDetail = new RspTransCenterDetail();
            BeanUtils.copyProperties( transaction, rspSellOrderDetail );

            resultList.add( rspSellOrderDetail );
        }
        return resultList;
    }

    @Override
    public RspBase<RspSellOrderDetail2> toBuySellOrderDetail( String userId, String transactionId ) {
        // 买家用户
        WalletUser buyer   = walletUserService.getById( userId );
        RspBase    rspBase = walletUserService.validWalletUser( buyer );
        if ( rspBase != null ) {
            return rspBase;
        }
        WalletTransaction walletTransaction = this.baseMapper.selectById( transactionId );
        if ( walletTransaction == null ) {
            return RspBase.businessError( "此挂单不存在" );
        }
        WalletUser seller = walletUserService.getById( walletTransaction.getUserId() );

        RspSellOrderDetail2 rspSellOrderDetail2 = new RspSellOrderDetail2();
        rspSellOrderDetail2.setBuyOrderNum( seller.getBuyOrderNum() );
        rspSellOrderDetail2.setSellOrderNum( seller.getSellOrderNum() );
        rspSellOrderDetail2.setLevel( seller.getLevel() );
        rspSellOrderDetail2.setHeadImg( seller.getHeadImg() );
        rspSellOrderDetail2.setNikeName( seller.getNickName() );
        rspSellOrderDetail2.setTransactionId( walletTransaction.getTransactionId() );
        rspSellOrderDetail2.setAmount( walletTransaction.getAmount() );
        rspSellOrderDetail2.setPayMethodTypes( walletTransaction.getPayMethodTypes() );
        rspSellOrderDetail2.setReceivedTimeMonth( walletTransaction.getReceivedTimeMonth() );
        rspSellOrderDetail2.setTransferTimeMonth( walletTransaction.getTransferTimeMonth() );
        rspSellOrderDetail2.setSuccessNumMonth( walletTransaction.getSuccessNumMonth() );
        rspSellOrderDetail2.setSuccessRateMonth( walletTransaction.getSuccessRateMonth() );
        rspSellOrderDetail2.setCreditRating( 5 );
        rspSellOrderDetail2.setCanSplit( walletTransaction.getCanSplit() );
        if ( walletTransaction.getCanSplit() ) {
            rspSellOrderDetail2.setMinBuyNum( walletTransaction.getMinBuyNum() );
        }

        List<WalletUserPayMethod> userPayMethods = walletUserPayMethodMapper.selectList( new QueryWrapper<WalletUserPayMethod>()
                .eq( "user_id", userId )
                .eq( "audit_status", 1 )
                .orderByDesc( "create_time" ) );

        Map<String, List<RspPayMethod2>> rspPayMethodMap = new HashMap<>();
        List<RspConfigBankList>    configBankList  = configBankListCache.getEffectList();
        for ( WalletUserPayMethod userPayMethod : userPayMethods ) {
            RspPayMethod2 rspPayMethod = new RspPayMethod2();
            BeanUtils.copyProperties( userPayMethod, rspPayMethod );
             for ( RspConfigBankList rspConfigBank : configBankList ) {
                 if( Objects.equals( userPayMethod.getBankId(), rspConfigBank.getId() ) ){
                     rspPayMethod.setBankName( rspConfigBank.getBankName() );
                 }
             }
            rspPayMethodMap.putIfAbsent( userPayMethod.getMethodType().name(), new ArrayList<>() );
            rspPayMethodMap.get( userPayMethod.getMethodType().name() ).add(  rspPayMethod );
        }
        rspSellOrderDetail2.setRspPayMethodMap( rspPayMethodMap );
        return RspBase.ok( rspSellOrderDetail2 );
    }

    @Override
    public RspBase<?> validTransaction( WalletTransaction walletTransaction ) {
        if ( walletTransaction == null ) {
            return RspBase.businessError( "此挂单不存在" );
        }
        if ( walletTransaction.getStatus() == 2 || walletTransaction.getAmount() <= 0 ) {
            return RspBase.businessError( "此挂单已售罄" );
        }
        if ( walletTransaction.getStatus() == 3 ) {
            return RspBase.businessError( "挂单交易已取消" );
        }
        return null;
    }
}




