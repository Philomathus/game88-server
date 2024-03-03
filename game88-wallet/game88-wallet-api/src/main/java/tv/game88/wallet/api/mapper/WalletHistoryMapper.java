package tv.game88.wallet.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.wallet.api.entity.WalletHistory;

import java.util.List;

public interface WalletHistoryMapper extends BaseMapper<WalletHistory> {

    List<WalletHistory> selectWalletHistoryList(WalletHistory walletHistory );

}
