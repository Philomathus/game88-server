package tv.game88.wallet.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.RspMessage;
import tv.game88.wallet.api.entity.WalletMessage;
import tv.game88.wallet.api.entity.WalletTransactionDetail;

import java.util.List;

/**
 * 站内信Service接口
 *
 * @author MengJun
 */
public interface WalletMessageService extends IService<WalletMessage> {
    /**
     * 查询站内信列表
     *
     * @param walletMessage 站内信
     *
     * @return 站内信集合
     */
    public List<WalletMessage> selectWalletMessageList( WalletMessage walletMessage );

    List<RspMessage> getMessageList( String userId );

    RspBase<?> setMessageRead( String userId, Long messageId );

    RspBase<?> setAllMessageRead( String userId );

    RspBase<Boolean> isNewMessage( String userId );

    void saveWalletMessage( WalletTransactionDetail walletTransactionDetail, boolean isSeller );
}