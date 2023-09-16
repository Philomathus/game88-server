package tv.game88.wallet.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.ReqSellCoins;
import tv.game88.wallet.api.entity.WalletTransaction;
import tv.game88.wallet.api.entity.WalletUser;
import tv.game88.wallet.api.mapper.WalletTransactionMapper;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.service.WalletTransactionService;

import javax.annotation.Resource;

/**
 * @author meng.jun
 * @description 针对表【wallet_transaction(钱包交易表)】的数据库操作Service实现
 * @createDate 2023-08-21 17:27:31
 */
@Service
public class WalletTransactionServiceImpl extends ServiceImpl<WalletTransactionMapper, WalletTransaction> implements WalletTransactionService {
    @Resource
    private WalletUserMapper walletUserMapper;

    @Override
    public RspBase<?> sellCoins( String userId, ReqSellCoins reqSellCoins ) {
        WalletUser walletUser = walletUserMapper.selectById( userId );
        RspBase<?> rspBase    = this.validWalletUser( walletUser );
        if ( rspBase != null ) {
            return rspBase;
        }
        if ( walletUser.getAmount() < reqSellCoins.getSellNum() ) {
            return RspBase.businessError( "您的G币不足,G币数量:" + walletUser.getAmount() );
        }
        return null;
    }

    private RspBase<?> validWalletUser( WalletUser walletUser ) {
        if ( walletUser == null ) {
            return RspBase.businessError( "钱包用户不存在" );
        }
        if ( walletUser.getStatus() != 1 ) {
            return RspBase.businessError( "用户状态异常,请联系客服" );
        }
        if ( walletUser.getIsVerified() < 2 ) {
            return RspBase.businessError( "用户未实名或实名未认证" );
        }
        return null;
    }
}




