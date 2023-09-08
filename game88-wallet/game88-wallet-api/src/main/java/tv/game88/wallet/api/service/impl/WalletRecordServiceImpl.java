package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.SpringUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigEnvCacheUtil;
import tv.game88.core.config.cache.GenerateOrderCacheUtils;
import tv.game88.wallet.api.cache.WalletMerchantCacheUtil;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletMerchant;
import tv.game88.wallet.api.entity.WalletRecord;
import tv.game88.wallet.api.manager.WalletFundManager;
import tv.game88.wallet.api.mapper.WalletRecordMapper;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.service.WalletRecordService;
import tv.game88.wallet.api.type.WalletUserFundEnum;
import tv.game88.wallet.api.vo.PlatformUser;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private ConfigEnvCacheUtil      configEnvCacheUtil;
    @Resource
    private WalletMerchantCacheUtil walletMerchantCacheUtil;
    @Resource
    private WalletFundManager       walletFundManager;
    @Resource
    private WalletUserMapper        walletUserMapper;

    @Override
    public RspPayResult payOrder( ReqDepositOrder reqDepositOrder ) throws Exception {
        WalletMerchant walletMerchant = walletMerchantCacheUtil.getWalletMerchantCache( reqDepositOrder.getMerchantId() );
        RspPayResult   rspPayResult   = this.validated( reqDepositOrder, walletMerchant, reqDepositOrder.getWalletAddress() );
        if ( rspPayResult != null ) {
            return rspPayResult;
        }

        // 先保存订单,等待会员主动请求支付并扣除会员金额, 再异步处理订单回调
        SpringUtils.getBean( WalletRecordService.class ).saveOrderAndSendTask( reqDepositOrder, walletMerchant, 1 );

        WalletRecord walletRecord = this.baseMapper.selectOne( new QueryWrapper<WalletRecord>()
                .eq( "merchant_id", reqDepositOrder.getMerchantId() ).eq( "order_no", reqDepositOrder.getOrderNo() ) );
        RspWalletRecordPay rspWalletRecord = new RspWalletRecordPay();
        BeanUtils.copyProperties( walletRecord, rspWalletRecord );

        Map<String, Object> reqquestMap = JsonUtil.object2Map( rspWalletRecord );
        rspWalletRecord.setSign( this.sign( reqquestMap, walletMerchant ) );

        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put( "merchant_id", reqDepositOrder.getMerchantId() );
        resultMap.put( "order_no", reqDepositOrder.getOrderNo() );
        resultMap.put( "walletAddress", reqDepositOrder.getWalletAddress() );

        long   t    = System.currentTimeMillis();
        String sign = AESCoder.encryptByKey( JsonUtil.object2Json( resultMap ), AESCoder.secretKey + t );

        rspWalletRecord.setPayUrl(
                configEnvCacheUtil.getConf( "pay_host_url" ) + "/common/toDepositOrder?s=" + sign + "&t=" + t );
        return RspPayResult.ok( rspWalletRecord );
    }

    @Override
    public RspPayResult withdrawOrder( ReqWithdrawOrder reqWithdrawOrder ) {
        WalletMerchant walletMerchant = walletMerchantCacheUtil.getWalletMerchantCache( reqWithdrawOrder.getMerchantId() );
        RspPayResult   rspPayResult   = this.validated( reqWithdrawOrder, walletMerchant, reqWithdrawOrder.getWalletAddress() );
        if ( rspPayResult != null ) {
            return rspPayResult;
        }
        if ( walletMerchant.getAmount().compareTo( reqWithdrawOrder.getAmount() ) < 0 ) {
            return RspPayResult.businessError( "商户余额不足:" + walletMerchant.getAmount() );
        }

        // 先保存订单并添加会员金额, 再异步处理订单回调
        SpringUtils.getBean( WalletRecordService.class ).saveOrderAndSendTask( reqWithdrawOrder, walletMerchant, 2 );

        WalletRecord walletRecord = this.baseMapper.selectOne( new QueryWrapper<WalletRecord>()
                .eq( "merchant_id", reqWithdrawOrder.getMerchantId() ).eq( "order_no", reqWithdrawOrder.getOrderNo() ) );
        RspWalletRecord rspWalletRecord = new RspWalletRecord();
        BeanUtils.copyProperties( walletRecord, rspWalletRecord );

        Map<String, Object> reqquestMap = JsonUtil.object2Map( rspWalletRecord );
        rspWalletRecord.setSign( this.sign( reqquestMap, walletMerchant ) );
        return RspPayResult.ok( rspWalletRecord );
    }

    @Override
    public RspPayResult orderQuery( ReqOrderQuery reqOrderQuery ) {
        WalletMerchant walletMerchant = walletMerchantCacheUtil.getWalletMerchantCache( reqOrderQuery.getMerchantId() );
        RspPayResult   rspPayResult   = this.validated( reqOrderQuery, walletMerchant, null );
        if ( rspPayResult != null ) {
            return rspPayResult;
        }
        WalletRecord walletRecord = this.baseMapper.selectOne( new QueryWrapper<WalletRecord>()
                .eq( "merchant_id", reqOrderQuery.getMerchantId() ).eq( "order_id", reqOrderQuery.getOrderNo() ) );
        if ( walletRecord == null ) {
            return RspPayResult.businessError( "订单号" + reqOrderQuery.getOrderNo() + "不存在" );
        }
        RspWalletRecord rspWalletRecord = new RspWalletRecord();
        BeanUtils.copyProperties( walletRecord, rspWalletRecord );

        Map<String, Object> reqquestMap = JsonUtil.object2Map( rspWalletRecord );
        rspWalletRecord.setSign( this.sign( reqquestMap, walletMerchant ) );
        return RspPayResult.ok( "订单查询成功", rspWalletRecord );
    }

    @Transactional( rollbackFor = Exception.class )
    @Override
    public void saveOrderAndSendTask( ReqOrderBase reqOrderBase, WalletMerchant walletMerchant, int tradeType ) {
        WalletRecord walletRecord = new WalletRecord();
        walletRecord.setTradeNo( GenerateOrderCacheUtils.me.getOrderIdNoTime( 32 ) );
        walletRecord.setStatus( 2 ); //0 处理失败，1 处理成功 ，2 处理中
        walletRecord.setMerchantId( reqOrderBase.getMerchantId() );
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

            // 添加会员金额
            walletFundManager.addWalletUserMoney( reqWithdrawOrder.getWalletAddress(), reqWithdrawOrder.getMerchantId(),
                    reqWithdrawOrder.getAmount(), WalletUserFundEnum.TRANSFER_IN,
                    "钱包用户资金转入" + reqWithdrawOrder.getAmount(), walletRecord.getTradeNo(), reqWithdrawOrder.getOrderNo() );
        }
        this.baseMapper.insert( walletRecord );

        if ( tradeType == 2 ) {
            // 设置redis队列,定时推送回调

        }
    }

    private RspPayResult validated( ReqOrderBase reqOrderBase, WalletMerchant walletMerchant, String walletAddress ) {
        if ( walletMerchant == null ) {
            return RspPayResult.businessError( "商户不存在" );
        }
        if ( walletMerchant.getStatus() == 0 ) {
            return RspPayResult.businessError( "此商户已封禁,请联系客服" );
        }
        Map<String, Object> reqquestMap = JsonUtil.object2Map( reqOrderBase );
        String              sign        = reqquestMap.remove( "sign" ).toString();

        if ( !sign.equalsIgnoreCase( this.sign( reqquestMap, walletMerchant ) ) ) {
            return RspPayResult.businessError( "验签失败!" );
        }

        QueryWrapper<WalletRecord> queryWrapper = new QueryWrapper<WalletRecord>()
                .eq( "merchant_id", reqOrderBase.getMerchantId() ).eq( "order_no", reqOrderBase.getOrderNo() );
        if ( this.baseMapper.exists( queryWrapper ) ) {
            return RspPayResult.businessError( "订单号" + reqOrderBase.getOrderNo() + "重复" );
        }
        if ( StringUtils.isNotBlank( walletAddress ) ) {
            PlatformUser platformUser = walletUserMapper.selectPlatformUserByUserId( walletAddress );
            if ( platformUser == null ) {
                return RspPayResult.businessError( "钱包用户不存在" );
            }
            if ( platformUser.getStatus() == 0 ) {
                return RspPayResult.businessError( "钱包用户已封禁" );
            }
        }
        return null;
    }

    private String sign( Map<String, Object> reqquestMap, WalletMerchant walletMerchant ) {
        reqquestMap.entrySet().removeIf( me -> me.getValue() == null || StringUtils.isBlank( me.getValue().toString() ) );
        reqquestMap.remove( "sign" );

        SortedMap<String, Object> bodyMap = new TreeMap<>( reqquestMap );

        StringBuilder sb = new StringBuilder();
        bodyMap.forEach( ( k, v ) -> sb.append( k ).append( "=" ).append( v ).append( "&" ) );
        String signStr = sb.substring( 0, sb.length() - 1 );
        return DigestUtils.md5Hex( signStr + "&key=" + AESCoder.decrypt( walletMerchant.getMd5Key() ) );
    }

    @Override
    public ModelAndView toDepositOrder( Map<String, Object> resultMap ) {
        String merchantId    = resultMap.getOrDefault( "merchant_id", "-1" ).toString();
        String orderNo       = resultMap.getOrDefault( "order_no", "" ).toString();
        String walletAddress = resultMap.getOrDefault( "walletAddress", "" ).toString();

        WalletRecord walletRecord = this.baseMapper.selectOne( new QueryWrapper<WalletRecord>().eq( "merchant_id", merchantId )
                                                                                               .eq( "order_no", orderNo ) );
        if ( walletRecord == null ) {
            throw new BusinessException( "订单不存在" );
        }
        BigDecimal userMoney = walletUserMapper.getUserMoney( walletAddress );
        if ( userMoney == null ) {
            throw new BusinessException( "钱包用户不存在" );
        }
        Map<String, Object> model = Maps.newHashMap();
        model.put( "orderNo", orderNo );
        model.put( "userMoney", userMoney );
        model.put( "orderMoney", walletRecord.getAmount() );
        return new ModelAndView("pay", model );
    }
}




