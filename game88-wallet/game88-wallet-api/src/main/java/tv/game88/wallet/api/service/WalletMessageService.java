package tv.game88.wallet.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.wallet.api.entity.WalletMessage;

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
}