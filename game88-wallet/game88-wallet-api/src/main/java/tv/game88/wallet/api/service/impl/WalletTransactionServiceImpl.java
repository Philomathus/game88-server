package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import tv.game88.wallet.api.entity.WalletTransaction;
import tv.game88.wallet.api.service.WalletTransactionService;
import tv.game88.wallet.api.mapper.WalletTransactionMapper;
import org.springframework.stereotype.Service;

/**
 * @author meng.jun
 * @description 针对表【wallet_transaction(钱包交易表)】的数据库操作Service实现
 * @createDate 2023-08-21 17:27:31
 */
@Service
public class WalletTransactionServiceImpl extends ServiceImpl<WalletTransactionMapper, WalletTransaction> implements WalletTransactionService {

}




