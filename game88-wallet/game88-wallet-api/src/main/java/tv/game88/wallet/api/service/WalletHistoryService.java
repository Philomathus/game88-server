package tv.game88.wallet.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.wallet.api.entity.WalletHistory;

import java.util.List;

public interface WalletHistoryService extends IService<WalletHistory> {
    List<WalletHistory> selectWalletHistoryList(WalletHistory walletHistory );
}
