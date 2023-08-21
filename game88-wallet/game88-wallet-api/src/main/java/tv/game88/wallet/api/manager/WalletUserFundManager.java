package tv.game88.wallet.api.manager;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.RedisUtils;
import tv.game88.wallet.api.mapper.WalletFundLogMapper;
import tv.game88.wallet.api.mapper.WalletUserMapper;
import tv.game88.wallet.api.type.WalletUserFundEnum;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
@Log4j2
public class WalletUserFundManager {
    @Resource
    private RedisUtils          redisUtils;
    @Resource
    private WalletFundLogMapper walletFundLogMapper;
    @Resource
    private WalletUserMapper    walletUserMapper;

    /**
     * 会员加钱
     *
     * @param userId   会员ID
     * @param addCount 增加的部分
     * @param fundEnum 交易类型
     */
    @Transactional( rollbackFor = Exception.class )
    public void addMemberMoney( String userId, BigDecimal addCount, WalletUserFundEnum fundEnum, String mark, String businessId
            , String markorder ) {
        if ( fundEnum.getType() < 0 ) {
            throw new BusinessException( "服务器异常" );
        }
    }
}
