package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.wallet.api.entity.WalletMessage;
import tv.game88.wallet.api.mapper.WalletMessageMapper;
import tv.game88.wallet.api.service.WalletMessageService;

import java.util.List;

/**
 * 站内信Service业务层处理
 *
 * @author MengJun
 */
@Service
public class WalletMessageServiceImpl extends ServiceImpl<WalletMessageMapper, WalletMessage> implements WalletMessageService {
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
}