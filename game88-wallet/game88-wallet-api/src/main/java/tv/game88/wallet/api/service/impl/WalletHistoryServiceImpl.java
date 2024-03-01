package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.wallet.api.entity.WalletHistory;
import tv.game88.wallet.api.mapper.WalletHistoryMapper;
import tv.game88.wallet.api.service.WalletHistoryService;

import java.util.List;

@Service
public class WalletHistoryServiceImpl extends ServiceImpl<WalletHistoryMapper, WalletHistory> implements WalletHistoryService {
    @Override
    public List<WalletHistory> selectWalletHistoryList(WalletHistory walletHistory) {
        return this.baseMapper.selectWalletHistoryList( walletHistory );
    }
}
