package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import tv.game88.common.utils.*;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.wallet.api.cache.WalletMerchantCacheUtil;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletMerchant;
import tv.game88.wallet.api.entity.WalletRecord;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.manager.WalletFundManager;
import tv.game88.wallet.api.mapper.WalletMerchantMapper;
import tv.game88.wallet.api.mapper.WalletRecordMapper;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.service.WalletRecordService;
import tv.game88.wallet.api.service.WalletUserService;
import tv.game88.wallet.api.type.WalletUserFundEnum;
import tv.game88.wallet.api.vo.PlatformUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author meng.jun
 * @description 针对表【wallet_record(钱包用户上下分记录)】的数据库操作Service实现
 * @createDate 2023-08-21 17:26:37
 */
@Service
@Log4j2
public class WalletRecordServiceImpl extends ServiceImpl<WalletRecordMapper, WalletRecord> implements WalletRecordService {
    @Resource
    private RedisUtils              redisUtils;
    @Resource
    private ConfigEnvCacheUtil      configEnvCacheUtil;
    @Resource
    private WalletMerchantCacheUtil walletMerchantCacheUtil;
    @Resource
    private WalletFundManager       walletFundManager;
    @Resource
    private WalletUserMapper        walletUserMapper;
    @Resource
    private WalletMerchantMapper    walletMerchantMapper;
    @Resource
    private WalletUserService       walletUserService;

    @Override
    public RspBase<RspWalletRecordPay> payOrder( ReqDepositOrder reqDepositOrder ) throws Exception {
        WalletMerchant walletMerchant = walletMerchantCacheUtil.getWalletMerchantCache( reqDepositOrder.getMerchantId() );
        RspBase        rspBase        = this.validated( reqDepositOrder, walletMerchant, reqDepositOrder.getWalletAddress() );
        if ( rspBase != null ) {
            return rspBase;
        }
        if ( StringUtils.isNotBlank( reqDepositOrder.getOrderNo() ) ) {
            QueryWrapper<WalletRecord> queryWrapper = new QueryWrapper<WalletRecord>()
                    .eq( "merchant_id", walletMerchant.getId() )
                    .eq( "order_no", reqDepositOrder.getOrderNo() );
            if ( this.baseMapper.exists( queryWrapper ) ) {
                return RspBase.businessError( "订单号" + reqDepositOrder.getOrderNo() + "重复" );
            }
        }

        // 先保存订单,等待会员主动请求支付并扣除会员金额, 再异步处理订单回调
        SpringUtils.getAopProxy( this ).saveOrderAndSendTask( reqDepositOrder, reqDepositOrder.getMerchantPlatformId(), 1 );

        WalletRecord walletRecord = this.baseMapper.selectOne( new QueryWrapper<WalletRecord>()
                .eq( "merchant_id", reqDepositOrder.getMerchantId() )
                .eq( "order_no", reqDepositOrder.getOrderNo() ) );
        RspWalletRecordPay rspWalletRecord = new RspWalletRecordPay();
        BeanUtils.copyProperties( walletRecord, rspWalletRecord );

        Map<String, Object> reqquestMap = JsonUtil.object2Map( rspWalletRecord );
        rspWalletRecord.setSign( this.sign( reqquestMap, walletMerchant ) );

        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put( "merchant_id", reqDepositOrder.getMerchantId() );
        resultMap.put( "order_no", reqDepositOrder.getOrderNo() );
        resultMap.put( "walletAddress", reqDepositOrder.getWalletAddress() );

        long   t    = System.currentTimeMillis();
        String sign = AESCoder.encryptByKeyUrl( JsonUtil.object2Json( resultMap ), DigestUtils.md5Hex( AESCoder.secretKey + t ) );

        rspWalletRecord.setPayUrl(
                configEnvCacheUtil.getConf( "pay_host_url" ) + "/api/common/toDepositOrder?s=" + sign + "&t=" + t );
        return RspBase.ok( rspWalletRecord );
    }

    @Override
    public RspBase<RspWalletRecord> withdrawOrder( ReqWithdrawOrder reqWithdrawOrder ) throws Exception {
        WalletMerchant walletMerchant = walletMerchantMapper.selectById( reqWithdrawOrder.getMerchantId() );
        RspBase        rspBase        = this.validated( reqWithdrawOrder, walletMerchant, reqWithdrawOrder.getWalletAddress() );
        if ( rspBase != null ) {
            return rspBase;
        }
        if ( StringUtils.isNotBlank( reqWithdrawOrder.getOrderNo() ) ) {
            QueryWrapper<WalletRecord> queryWrapper = new QueryWrapper<WalletRecord>()
                    .eq( "merchant_id", walletMerchant.getId() )
                    .eq( "order_no", reqWithdrawOrder.getOrderNo() );
            if ( this.baseMapper.exists( queryWrapper ) ) {
                return RspBase.businessError( "订单号" + reqWithdrawOrder.getOrderNo() + "重复" );
            }
        }
        if ( walletMerchant.getAmount().compareTo( new BigDecimal( reqWithdrawOrder.getAmount() ) ) < 0 ) {
            return RspBase.businessError( "商户余额不足:" + walletMerchant.getAmount() );
        }

        // 先保存订单并添加会员金额, 再异步处理订单回调
        SpringUtils.getAopProxy( this ).saveOrderAndSendTask( reqWithdrawOrder, reqWithdrawOrder.getMerchantPlatformId(), 2 );

        WalletRecord walletRecord = this.baseMapper.selectOne( new QueryWrapper<WalletRecord>()
                .eq( "merchant_id", reqWithdrawOrder.getMerchantId() )
                .eq( "order_no", reqWithdrawOrder.getOrderNo() ) );
        RspWalletRecord rspWalletRecord = new RspWalletRecord();
        BeanUtils.copyProperties( walletRecord, rspWalletRecord );

        Map<String, Object> reqquestMap = JsonUtil.object2Map( rspWalletRecord );
        rspWalletRecord.setSign( this.sign( reqquestMap, walletMerchant ) );
        return RspBase.ok( rspWalletRecord );
    }

    @Override
    public RspBase<RspWalletRecord> orderQuery( ReqOrderQuery reqOrderQuery ) throws Exception {
        WalletMerchant walletMerchant = walletMerchantCacheUtil.getWalletMerchantCache( reqOrderQuery.getMerchantId() );
        RspBase        rspBase        = this.validated( reqOrderQuery, walletMerchant, null );
        if ( rspBase != null ) {
            return rspBase;
        }
        WalletRecord walletRecord = this.baseMapper.selectOne( new QueryWrapper<WalletRecord>()
                .eq( "merchant_id", reqOrderQuery.getMerchantId() )
                .eq( "order_no", reqOrderQuery.getOrderNo() ) );
        if ( walletRecord == null ) {
            return RspBase.businessError( "订单号" + reqOrderQuery.getOrderNo() + "不存在" );
        }
        RspWalletRecord rspWalletRecord = new RspWalletRecord();
        BeanUtils.copyProperties( walletRecord, rspWalletRecord );

        Map<String, Object> reqquestMap = JsonUtil.object2Map( rspWalletRecord );
        rspWalletRecord.setSign( this.sign( reqquestMap, walletMerchant ) );
        return RspBase.ok( "订单查询成功", rspWalletRecord );
    }

    @Transactional( rollbackFor = Exception.class )
    public void saveOrderAndSendTask( ReqOrderBase reqOrderBase, String merchantPlatformId, int tradeType ) {
        WalletRecord walletRecord = new WalletRecord();
        walletRecord.setTradeNo( GenerateOrderCacheUtils.me.getOrderIdNoTime( 32 ) );
        walletRecord.setStatus( 2 ); //0 处理失败，1 处理成功 ，2 处理中
        walletRecord.setNotifyStatus( 0 ); //0 无需通知, 1 通知成功, 2 通知失败
        walletRecord.setMerchantId( reqOrderBase.getMerchantId() );
        walletRecord.setMerchantPlatformId( merchantPlatformId );
        walletRecord.setOrderNo( reqOrderBase.getOrderNo() );
        walletRecord.setTradeType( tradeType );
        walletRecord.setCreateTime( LocalDateTime.now() );
        walletRecord.setUpdateTime( walletRecord.getCreateTime() );
        if ( tradeType == 1 ) {
            ReqDepositOrder reqDepositOrder = ( ReqDepositOrder ) reqOrderBase;
            walletRecord.setRemark( reqDepositOrder.getRemark() );
            walletRecord.setAmount( reqDepositOrder.getAmount() );
            walletRecord.setNotifyUrl( reqDepositOrder.getNotifyUrl() );
            walletRecord.setUserId( reqDepositOrder.getWalletAddress() );
        } else {
            ReqWithdrawOrder reqWithdrawOrder = ( ReqWithdrawOrder ) reqOrderBase;
            walletRecord.setRemark( reqWithdrawOrder.getRemark() );
            walletRecord.setAmount( reqWithdrawOrder.getAmount() );
            walletRecord.setNotifyUrl( reqWithdrawOrder.getNotifyUrl() );
            walletRecord.setUserId( reqWithdrawOrder.getWalletAddress() );
            walletRecord.setStatus( 1 );

            // 添加会员金额
            WalletUserFundEnum fundEnum = WalletUserFundEnum.WITHDRAW_IN;
            String             mark     = "用户" + fundEnum.getDes() + reqWithdrawOrder.getAmount();
            walletFundManager.addWalletUserMoney( reqWithdrawOrder.getWalletAddress(), reqWithdrawOrder.getMerchantId(),
                    reqWithdrawOrder.getAmount(), fundEnum, mark, walletRecord.getTradeNo(), reqWithdrawOrder.getOrderNo() );
        }
        this.baseMapper.insert( walletRecord );

        if ( tradeType == 2 ) {
            // 设置redis队列,定时推送回调
            this.sendRedisCallbackTask( walletRecord.getTradeNo(), walletRecord.getNotifyUrl() );
        }
    }

    @Override
    public RspBase validated( Object obj, WalletMerchant walletMerchant, String walletAddress ) throws Exception {
        if ( walletMerchant == null ) {
            return RspBase.businessError( "商户不存在" );
        }
        if ( walletMerchant.getStatus() == 0 ) {
            return RspBase.businessError( "此商户已封禁,请联系客服" );
        }
        Map<String, Object> reqquestMap = JsonUtil.object2Map( obj );
        String              sign        = reqquestMap.remove( "sign" ).toString();

        if ( !sign.equalsIgnoreCase( this.sign( reqquestMap, walletMerchant ) ) ) {
            return RspBase.businessError( "验签失败!" );
        }

        if ( StringUtils.isNotBlank( walletAddress ) ) {
            PlatformUser platformUser = walletUserMapper.selectPlatformUserByUserId( walletAddress );
            if ( platformUser == null ) {
                return RspBase.businessError( "钱包用户不存在" );
            }
            if ( platformUser.getStatus() == 0 ) {
                return RspBase.businessError( "钱包用户已封禁" );
            }
        }
        return null;
    }

    private String sign( Map<String, Object> reqquestMap, WalletMerchant walletMerchant ) throws Exception {
        reqquestMap.entrySet().removeIf( me -> me.getValue() == null || StringUtils.isBlank( me.getValue().toString() ) );
        reqquestMap.remove( "sign" );

        SortedMap<String, Object> bodyMap = new TreeMap<>( reqquestMap );

        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String signStr = sb.substring( 0, sb.length() - 1 );
        return DigestUtils.md5Hex( signStr + "&key=" + AESCoder.decrypt( walletMerchant.getMd5Key() ) );
    }

    @Override
    public ModelAndView toDepositOrder( String s, long t ) throws Exception {
        String              data      = AESCoder.decryptByKey( s, DigestUtils.md5Hex( AESCoder.secretKey + t ) );
        Map<String, Object> resultMap = JsonUtil.json2Map( data );

        long   merchantId    = Long.parseLong( resultMap.getOrDefault( "merchant_id", "-1" ).toString() );
        String orderNo       = resultMap.getOrDefault( "order_no", "" ).toString();
        String walletAddress = resultMap.getOrDefault( "walletAddress", "" ).toString();

        Map<String, Object> model = Maps.newHashMap();
        model.put( "orderNo", orderNo );
        model.put( "userMoney", 0 );
        model.put( "orderMoney", 0 );
        model.put( "errorText", "" );

        WalletRecord walletRecord = this.baseMapper.selectOne( new QueryWrapper<WalletRecord>()
                .eq( "merchant_id", merchantId )
                .eq( "order_no", orderNo ) );
        if ( walletRecord == null ) {
            model.put( "errorText", "订单不存在" );
            return new ModelAndView( "pay", model );
        }
        model.put( "orderMoney", walletRecord.getAmount() );
        WalletMerchant walletMerchant = walletMerchantCacheUtil.getWalletMerchantCache( merchantId );
        if ( walletMerchant == null ) {
            model.put( "errorText", "商户不存在" );
            return new ModelAndView( "pay", model );
        }
        if ( walletMerchant.getStatus() == 0 ) {
            model.put( "errorText", "此商户已封禁,请联系客服" );
            return new ModelAndView( "pay", model );
        }
        WalletUser walletUser = walletUserMapper.selectById( walletAddress );
        if ( walletUser == null ) {
            model.put( "errorText", "钱包用户不存在" );
            return new ModelAndView( "pay", model );
        }
        if ( walletUser.getStatus() != 1 ) {
            model.put( "errorText", "用户状态异常,请联系客服" );
            return new ModelAndView( "pay", model );
        }
        if ( walletUser.getIsVerified() < 2 ) {
            model.put( "errorText", "用户未实名或实名未认证" );
            return new ModelAndView( "pay", model );
        }
        model.put( "userMoney", walletUser.getAmount() + walletUser.getFrozenAmount() );
        return new ModelAndView( "pay", model );
    }

    @Override
    public RspBase<?> payDepositOrder( ReqPayDepositOrder reqPayDepositOrder ) throws Exception {
        String data = AESCoder.decryptByKey( reqPayDepositOrder.getS(), DigestUtils.md5Hex(
                AESCoder.secretKey + reqPayDepositOrder.getT() ) );

        Map<String, Object> resultMap     = JsonUtil.json2Map( data );
        long                merchantId    = Long.parseLong( resultMap.getOrDefault( "merchant_id", "-1" ).toString() );
        String              orderNo       = resultMap.getOrDefault( "order_no", "" ).toString();
        String              walletAddress = resultMap.getOrDefault( "walletAddress", "" ).toString();

        WalletRecord walletRecord = this.baseMapper.selectOne( new QueryWrapper<WalletRecord>()
                .eq( "merchant_id", merchantId )
                .eq( "order_no", orderNo ) );
        if ( walletRecord == null ) {
            return RspBase.businessError( "订单不存在" );
        }
        WalletMerchant walletMerchant = walletMerchantCacheUtil.getWalletMerchantCache( merchantId );
        if ( walletMerchant == null ) {
            return RspBase.businessError( "商户不存在" );
        }
        if ( walletMerchant.getStatus() == 0 ) {
            return RspBase.businessError( "此商户已封禁,请联系客服" );
        }
        WalletUser walletUser = walletUserMapper.selectById( walletAddress );
        if ( walletUser == null ) {
            return RspBase.businessError( "钱包用户不存在" );
        }
        if ( walletUser.getStatus() != 1 ) {
            return RspBase.businessError( "用户状态异常,请联系客服" );
        }
        if ( walletUser.getIsVerified() < 2 ) {
            return RspBase.businessError( "用户未实名或实名未认证" );
        }
        RspBase rspBase = walletUserService.validatedPasswordTimes( reqPayDepositOrder.getP(), walletUser );
        if ( rspBase != null ) {
            return rspBase;
        }
        if ( ( walletUser.getAmount() + walletUser.getFrozenAmount() ) < walletRecord.getAmount() ) {
            return RspBase.businessError( "用户余额不足" );
        }
        // 修改用户支付订单状态并扣除会员金额,然后异步通知支付回调
        SpringUtils.getAopProxy( this ).updateOrderAndSendTask( walletRecord );

        return RspBase.ok( "支付成功", null );
    }

    @Transactional( rollbackFor = Exception.class )
    public void updateOrderAndSendTask( WalletRecord walletRecord ) {
        WalletRecord update = new WalletRecord();
        update.setTradeNo( walletRecord.getTradeNo() );
        update.setStatus( 1 );
        update.setNotifyStatus( 0 );
        update.setUpdateTime( LocalDateTime.now() );
        this.baseMapper.updateById( update );
        // 扣除会员金额
        WalletUserFundEnum fundEnum = WalletUserFundEnum.DEPOSIT_OUT;
        String             mark     = "用户" + fundEnum.getDes() + walletRecord.getAmount();
        walletFundManager.reduceWalletUserMoney( walletRecord.getUserId(), walletRecord.getMerchantId(),
                walletRecord.getAmount(), fundEnum, mark, walletRecord.getTradeNo(), walletRecord.getOrderNo() );

        // 设置redis队列,定时推送回调
        this.sendRedisCallbackTask( walletRecord.getTradeNo(), walletRecord.getNotifyUrl() );
    }

    private void sendRedisCallbackTask( String tradeNo, String notifyUrl ) {
        if ( StringUtils.isBlank( notifyUrl ) ) {
            return;
        }
        long timestamp = LocalDateTimeUtils.localDateToTimestamp( LocalDateTime.now().plusSeconds( 5 ) );

        Map<String, Object> map = Maps.newHashMap();
        map.put( "time", timestamp );
        map.put( "num", 0 );
        map.put( "notifyUrl", notifyUrl );
        redisUtils.hSet( Constants.WALLET_PREX + "callback:orderNo", tradeNo, JsonUtil.object2Json( map ) );
    }


    @Override
    public RspWalletRecord getRspData( String tradeNo ) throws Exception {
        WalletRecord    walletRecord    = this.baseMapper.selectById( tradeNo );
        WalletMerchant  walletMerchant  = walletMerchantCacheUtil.getWalletMerchantCache( walletRecord.getMerchantId() );
        RspWalletRecord rspWalletRecord = new RspWalletRecord();
        BeanUtils.copyProperties( walletRecord, rspWalletRecord );
        Map<String, Object> reqquestMap = JsonUtil.object2Map( rspWalletRecord );
        rspWalletRecord.setSign( this.sign( reqquestMap, walletMerchant ) );
        log.error( JsonUtil.object2Json( rspWalletRecord ) );
        return rspWalletRecord;
    }

    @Override
    public List<WalletRecord> getWalletRecordList( WalletRecord walletRecord ) {
        return this.baseMapper.selectWalletRecord( walletRecord );
    }
}




