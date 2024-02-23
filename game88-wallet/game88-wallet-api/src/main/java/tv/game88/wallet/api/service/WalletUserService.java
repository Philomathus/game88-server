package tv.game88.wallet.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.constants.ReqConstant;
import tv.game88.wallet.api.dto.*;
import tv.game88.wallet.api.entity.WalletUser;

import java.util.List;

/**
 * @author meng.jun
 * @description 针对表【wallet_user(钱包用户表)】的数据库操作Service
 * @createDate 2023-08-21 17:32:24
 */
public interface WalletUserService extends IService<WalletUser> {

    /**
     * 查询钱包用户列表
     *
     * @param walletUser 钱包用户
     *
     * @return 钱包用户集合
     */
    List<WalletUser> selectWalletUserList( WalletUser walletUser );

    RspBase<?> sendSmsVerifyCode( Phone phone );

    RspBase<RspMember> login( MobileLogin mobileLogin, Integer dev, String loginUrl );

    RspBase<RspMember> register( MobileLogin mobileLogin, Integer dev, String loginUrl );

    RspBase<?> embeddedLogin( ReqEmbeddedLogin reqEmbeddedLogin );

    RspBase<?> resetPasswd( ReqResetPasswd reqResetPasswd, String userId );

    RspBase<RspAmount> getAmount( String userId );

    RspBase<RspMember> getUserInfo( String userId );

    RspBase<?> fundPassSet( String userId, ReqFundPass reqFundPass );

    List<RspLogFund> getFundDetails( String userId, ReqLogFund reqLogFund );

    RspBase<?> personalTransfer( String userId, ReqPersonalTransfer reqPersonalTransfer );

    /**
     * 校验资金密码
     *
     * @param rawPassword 资金密码6位数字
     * @param walletUser  钱包用户
     *
     * @return 错误结果
     */
    RspBase validatedPasswordTimes( String rawPassword, WalletUser walletUser );

    void processFundTransfer( String userId, ReqPersonalTransfer reqPersonalTransfer );

    RspBase<?> validWalletUser( WalletUser walletUser );

    RspBase<?> verifyIdCard( String userId, ReqVerifyIdCard reqVerifyIdCard );

    RspBase<?> setPassword( String userId, ReqConstant.ReqSetPasswd reqSetPasswd );

    RspBase<?> resetFunPassword( String userId, ReqConstant.ReqResetFundPasswd reqResetFundPasswd );
}
