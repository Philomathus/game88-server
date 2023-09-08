package tv.game88.wallet.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author meng.jun
 * @description 针对表【wallet_user(钱包用户表)】的数据库操作Service
 * @createDate 2023-08-21 17:32:24
 */
public interface WalletUserService extends IService<WalletUser> {

    RspBase<?> sendSmsVerifyCode( Phone phone );

    RspBase<RspMember> login( MobileLogin mobileLogin, Integer dev, String loginUrl );

    RspBase<RspMember> register( MobileLogin mobileLogin, Integer dev, String loginUrl );

    RspPayResult embeddedLogin( ReqEmbeddedLogin reqEmbeddedLogin );
}
