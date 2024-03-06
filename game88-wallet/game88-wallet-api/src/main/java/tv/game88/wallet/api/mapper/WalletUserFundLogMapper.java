package tv.game88.wallet.api.mapper;

import org.apache.ibatis.annotations.Param;
import tv.game88.wallet.api.dto.ReqLogFund;
import tv.game88.wallet.api.dto.RspLogFund;
import tv.game88.wallet.api.entity.WalletUserFundLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author meng.jun
 * @description 针对表【wallet_user_fund_log(用户资金日志表)】的数据库操作Mapper
 * @createDate 2023-08-21 17:23:44
 * @Entity tv.game88.wallet.api.entity.WalletUserFundLog
 */
public interface WalletUserFundLogMapper extends BaseMapper<WalletUserFundLog> {

    List<RspLogFund> findLogFundList( @Param( "userId" ) String userId, @Param( "req" ) ReqLogFund reqLogFund, @Param(
            "beginDay" ) String beginDay, @Param( "endDay" ) String endDay );

    List<WalletUserFundLog> selectWalletUserFundLog (WalletUserFundLog walletUserFundLog );
}




