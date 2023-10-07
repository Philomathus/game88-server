package tv.game88.wallet.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.ReqPayMethod;
import tv.game88.wallet.api.dto.RspPayMethod;
import tv.game88.wallet.api.entity.WalletUserPayMethod;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author meng.jun
 * @description 针对表【wallet_user_pay_method】的数据库操作Service
 * @createDate 2023-08-21 17:33:52
 */
public interface WalletUserPayMethodService extends IService<WalletUserPayMethod> {

    RspBase<?> bindNewPayMethod( String userId, ReqPayMethod reqPayMethod );

    RspBase<?> unBindPayMethod( String userId, int payMethodId );

    RspBase<Map<String, List<RspPayMethod>>> getPayMethod( String userId );

    RspBase<Boolean> hasPayMethod( String userId );
}
