package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.constants.ConstantsWallet;
import tv.game88.wallet.api.dto.RspMessage;
import tv.game88.wallet.api.entity.WalletMessage;
import tv.game88.wallet.api.mapper.WalletMessageMapper;
import tv.game88.wallet.api.service.WalletMessageService;
import tv.game88.wallet.api.type.WalletMessageEnum;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 站内信Service业务层处理
 *
 * @author MengJun
 */
@Service
public class WalletMessageServiceImpl extends ServiceImpl<WalletMessageMapper, WalletMessage> implements WalletMessageService {
    @Resource
    private RedisUtils redisUtils;

    /**
     * 查询站内信列表
     *
     * @param walletMessage 站内信
     *
     * @return 站内信
     */
    @Override
    public List<WalletMessage> selectWalletMessageList( WalletMessage walletMessage ) {
        return this.baseMapper.selectWalletMessageList( walletMessage );
    }

    @Override
    public List<RspMessage> getMessageList( String userId ) {
        List<WalletMessage> list = new QueryChainWrapper<>( this.baseMapper ).eq( "receiver_user_id", userId ).or()
                                                                             .eq( "type", WalletMessageEnum.system )
                                                                             .orderByDesc( "create_time" ).list();
        if ( !list.isEmpty() ) {
            redisUtils.unlink( ConstantsWallet.MESSAGE_PERSONAL_PROMPT + userId );
        }
        return list.stream().map( hn -> {
            if ( hn.getType() == WalletMessageEnum.system ) {
                hn.setIsRead( redisUtils.sIsMember( ConstantsWallet.MESSAGE_SYSTEM_IS_READ + hn.getId(), userId ) );
            }
            RspMessage rsp = new RspMessage();
            BeanUtils.copyProperties( hn, rsp );
            return rsp;
        } ).collect( Collectors.toList() );
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
    public RspBase<Boolean> isNewMessage( String userId ) {
        return RspBase.ok( redisUtils.exists( ConstantsWallet.MESSAGE_PERSONAL_PROMPT + userId ) );
    }
}