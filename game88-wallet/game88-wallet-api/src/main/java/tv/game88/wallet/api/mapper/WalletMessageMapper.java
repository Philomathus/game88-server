package tv.game88.wallet.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.wallet.api.dto.RspWalletMessage;
import tv.game88.wallet.api.entity.WalletMessage;

import java.util.List;

/**
 * 站内信Mapper接口
 *
 * @author MengJun
 */
public interface WalletMessageMapper extends BaseMapper<WalletMessage> {

    /**
     * 查询站内信列表
     *
     * @param walletMessage 站内信
     *
     * @return 站内信集合
     */
    public List<RspWalletMessage> selectWalletMessageList(WalletMessage walletMessage );
}