package tv.game88.wallet.api.mapper;

import tv.game88.wallet.api.entity.WalletTransaction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author meng.jun
* @description 针对表【wallet_transaction(钱包交易表)】的数据库操作Mapper
* @createDate 2023-08-21 17:27:31
* @Entity tv.game88.wallet.api.entity.WalletTransaction
*/
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {
    public List<WalletTransaction> selectWalletTransactionList( WalletTransaction walletTransaction );
}




