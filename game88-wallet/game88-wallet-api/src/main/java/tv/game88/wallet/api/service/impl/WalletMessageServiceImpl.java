package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.dto.RspMessage;
import tv.game88.wallet.api.dto.RspWalletMessage;
import tv.game88.wallet.api.dto.SseStreamTransDetailMessage;
import tv.game88.wallet.api.entity.WalletMessage;
import tv.game88.wallet.api.entity.WalletTransactionDetail;
import tv.game88.wallet.api.mapper.WalletMessageMapper;
import tv.game88.wallet.api.service.WalletMessageService;
import tv.game88.wallet.api.type.WalletMessageEnum;
import tv.game88.wallet.api.type.WalletTransEnum;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 站内信Service业务层处理
 *
 * @author MengJun
 */
@Service
public class WalletMessageServiceImpl extends ServiceImpl<WalletMessageMapper, WalletMessage> implements WalletMessageService {
    @Resource
    private RedisUtils          redisUtils;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询站内信列表
     *
     * @param walletMessage 站内信
     *
     * @return 站内信
     */
    @Override
    public List<RspWalletMessage> selectWalletMessageList(WalletMessage walletMessage ) {
        return this.baseMapper.selectWalletMessageList( walletMessage );
    }

    @Override
    public List<RspMessage> getMessageList( String userId ) {
        List<WalletMessage> list = new LambdaQueryChainWrapper<>( this.baseMapper )
                .eq( WalletMessage::getReceiverUserId, userId )
                .or()
                .eq( WalletMessage::getType, WalletMessageEnum.system )
                .orderByDesc( WalletMessage::getCreateTime )
                .list();
        if ( !list.isEmpty() ) {
            redisUtils.unlink( ConstantsWallet.MESSAGE_PERSONAL_PROMPT + userId );
        }
        return list.stream().map( hn -> {
            if ( hn.getType() == WalletMessageEnum.system ) {
                hn.setIsRead( redisUtils.sIsMember( ConstantsWallet.MESSAGE_SYSTEM_IS_READ + hn.getId(), userId ) );
            }
            RspMessage rsp = new RspMessage();
            BeanUtils.copyProperties( hn, rsp );
            rsp.setDate( hn.getCreateTime().format( LocalDateTimeUtils.YYYY_MM_DD_FORMATTER ) );
            return rsp;
        } ).toList();
    }

    @Override
    public RspBase<?> setMessageRead( String userId, Long messageId ) {
        WalletMessage walletMessage = this.baseMapper.selectById( messageId );
        if ( walletMessage == null ) {
            return RspBase.businessError( "消息不存在" );
        }
        if ( walletMessage.getType() == WalletMessageEnum.personal ) {
            if ( !userId.equals( walletMessage.getReceiverUserId() ) ) {
                return RspBase.businessError( "此消息不属于您" );
            }
            WalletMessage update = new WalletMessage();
            update.setId( messageId );
            update.setIsRead( true );
            int i = this.baseMapper.updateById( update );
            if ( i <= 0 ) {
                return RspBase.businessError( "设置已读失败,请重试" );
            }
        } else {
            redisUtils.sAdd( ConstantsWallet.MESSAGE_SYSTEM_IS_READ + messageId, userId );
        }
        return RspBase.ok( "设置已读成功" );
    }

    @Override
    public RspBase<?> setAllMessageRead( String userId ) {
        new LambdaQueryChainWrapper<>( this.baseMapper )
                .eq( WalletMessage::getType, WalletMessageEnum.system )
                .list()
                .forEach( wm -> redisUtils.sAdd( ConstantsWallet.MESSAGE_SYSTEM_IS_READ + wm.getId(), userId ) );
        this.update( new LambdaUpdateWrapper<WalletMessage>()
                .set( WalletMessage::getIsRead, true )
                .eq( WalletMessage::getReceiverUserId, userId )
                .eq( WalletMessage::getIsRead, false ) );
        return RspBase.ok();
    }

    @Override
    public RspBase<Boolean> isNewMessage( String userId ) {
        return RspBase.ok( redisUtils.exists( ConstantsWallet.MESSAGE_PERSONAL_PROMPT + userId ) );
    }

    @Async
    @Override
    public void saveWalletMessage( WalletTransactionDetail walletTransactionDetail, boolean isSeller ) {
        String titleText;
        String actionText;
        String orderText;

        WalletTransEnum walletTransEnum = walletTransactionDetail.getStatus();
        String          transDetailId   = walletTransactionDetail.getTransDetailId();
        String          transactionId   = walletTransactionDetail.getTransactionId();
        String          receiverUserId  = isSeller ? walletTransactionDetail.getSellerId() : walletTransactionDetail.getBuyerId();
        switch ( walletTransEnum ) {
        case BUYER_CONFIRM_BUY -> {
            titleText  = "您有新的交易订单";
            orderText  = "挂单";
            actionText = "有买家发起交易请求，请前往＂我的" + orderText + "＂进行处理或查看";
        }
        case SELLER_CONFIRM_TRANS -> {
            titleText  = "卖家确认交易";
            orderText  = "订单";
            actionText = "经卖家确认可交易，请前往＂我的" + orderText + "＂进行下一步处理";
        }
        case SELLER_CANCEL -> {
            titleText  = "卖家取消交易";
            orderText  = "订单";
            actionText = "已被卖家取消交易，请前往＂" + orderText + "＂查看";
        }
        case BUYER_CONFIRM_TRANSFER -> {
            titleText  = "买家确认转账";
            orderText  = "订单";
            actionText = "已被买家确认已转账，请前往＂我的" + orderText + "＂进行处理";
        }
        case BUYER_CANCEL -> {
            titleText  = "买家取消交易";
            orderText  = "订单";
            actionText = "已被买家取消交易，请前往＂我的" + orderText + "＂查看";
        }
        case SELLER_CONFIRM_TRANSFER -> {
            titleText  = "卖家确认转币";
            orderText  = "订单";
            actionText = "经卖家确认收款并转币，请前往＂我的" + orderText + "＂查看";
        }
        case SELLER_NOT_RECEIVED -> {
            titleText  = "卖家未收到转账";
            orderText  = "订单";
            actionText = "已确认转币，但卖家确认长时间未收到转账，此单已转由平台管理员确认，您可以联系客服提供更加充足的凭证。";
        }
        case SYSTEM_CONFIRM_TRANSFER -> {
            titleText  = "系统确认转币";
            orderText  = "订单";
            actionText = "经系统确认收款并转币，请前往＂我的" + orderText + "＂查看";
        }
        default -> {
            titleText  = "标题";
            orderText  = "订单";
            actionText = "";
        }
        }

        WalletMessage walletMessage = new WalletMessage();
        walletMessage.setIsRead( false );
        walletMessage.setType( WalletMessageEnum.personal );
        walletMessage.setTitle( titleText );
        String content = String.format( "尊敬的用户！您的%s（单号：%s）%s", orderText, transDetailId, actionText );
        walletMessage.setContent( content );
        walletMessage.setReceiverUserId( receiverUserId );
        walletMessage.setCreateTime( LocalDateTime.now() );
        this.baseMapper.insert( walletMessage );

        stringRedisTemplate.convertAndSend(
                ConstantsWallet.SSE_MEMBER_CHANNEL + receiverUserId, JsonUtil.object2Json( SseStreamTransDetailMessage
                        .builder()
                        .transDetailId( transDetailId )
                        .transactionId( transactionId )
                        .isSeller( isSeller )
                        .walletTransEnum( walletTransEnum )
                        .time( LocalDateTimeUtils.format( LocalDateTime.now() ) )
                        .build() ) );
    }
}
