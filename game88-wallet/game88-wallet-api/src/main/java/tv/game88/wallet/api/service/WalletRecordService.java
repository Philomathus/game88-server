package tv.game88.wallet.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.servlet.ModelAndView;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletMerchant;
import tv.game88.wallet.api.entity.WalletRecord;

import java.util.Map;

/**
 * @author meng.jun
 * @description 针对表【wallet_record(钱包用户上下分记录)】的数据库操作Service
 * @createDate 2023-08-21 17:26:37
 */
public interface WalletRecordService extends IService<WalletRecord> {

    RspPayResult payOrder( ReqDepositOrder reqDepositOrder ) throws Exception;

    RspPayResult withdrawOrder( ReqWithdrawOrder reqWithdrawOrder );

    RspPayResult orderQuery( ReqOrderQuery reqOrderQuery );

    void saveOrderAndSendTask( ReqOrderBase reqOrderBase, WalletMerchant walletMerchant, int tradeType );

    ModelAndView toDepositOrder( Map<String, Object> resultMap );
}
