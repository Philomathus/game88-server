package tv.game88.wallet.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.servlet.ModelAndView;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletMerchant;
import tv.game88.wallet.api.entity.WalletRecord;

import java.util.List;

/**
 * @author meng.jun
 * @description 针对表【wallet_record(钱包用户上下分记录)】的数据库操作Service
 * @createDate 2023-08-21 17:26:37
 */
public interface WalletRecordService extends IService<WalletRecord> {

    RspBase<RspWalletRecordPay> payOrder( ReqDepositOrder reqDepositOrder ) throws Exception;

    RspBase<RspWalletRecord> withdrawOrder( ReqWithdrawOrder reqWithdrawOrder );

    RspBase<RspWalletRecord> orderQuery( ReqOrderQuery reqOrderQuery );

    RspBase validated( Object obj, WalletMerchant walletMerchant, String walletAddress, String orderNo );

    ModelAndView toDepositOrder( String s, long t ) throws Exception;

    RspBase<?> payDepositOrder( ReqPayDepositOrder reqPayDepositOrder ) throws Exception;

    RspWalletRecord getRspData( String tradeNo );

    List<WalletRecord> getWalletRecordList( WalletRecord walletRecord );
}
