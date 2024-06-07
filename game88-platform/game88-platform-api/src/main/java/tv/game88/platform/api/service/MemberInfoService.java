package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.dto.*;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.platform.api.dto.*;
import tv.game88.platform.api.entity.MemberVipGift;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MemberInfoService extends IService<MemberInfo> {
    RspInit getLoginInit( Integer dev, String version );

    RspManUpdateVersion checkManUpdateVersion( Integer dev, String version );

    RspBase<RspMember> login( MobileLogin mobileLogin, Integer dev, String version, String loginUrl );

    RspBase<RspMember> loginDevice( MobileLogin mobileLogin, Integer dev, String version, String loginUrl );

    RspBase<RspMember> register( MobileLogin mobileLogin, Integer dev, String version, String loginUrl ) throws Exception;

    RspBase<RspMember> usernameRegister( MobileLogin mobileLogin, Integer dev, String version, String loginUrl );

    RspBase<RspMember> usernameLogin( MobileLogin mobileLogin, Integer dev, String version, String loginUrl );

    RspBase<?> sendSmsVerifyCode( Phone phone );

    RspBase<?> addMemberMoneyOnly( String ip, String userName, ReqAddScore req );

    List<MemberInfo> selectMemberInfoList( MemberInfo memberInfo );

    Map listCount( MemberInfo memberInfo );

    RspBase<?> updateMobile( String oldMobile, String newMobile, String memberId );

    List<MemberCard> selectMemberCardList( String memberId );

    BigDecimal getHistoryRecharge( String memberId );

    boolean repairMemberBcode( String memberId );

    RspBase<?> unbindCard( MemberCard memberCard );

    RspBase<?> changeBank( MemberCard memberCard );

    RspBase<?> personalReport( String startTime, String endTime, String memberId );

    RspBase<?> boxDish( String memberId );

    RspBase<?> updateVip( String memberId, Integer vip, String nickName );

    RspBase<?> memberBoxPassIsOpen( String memberId );

    RspBase<?> memberBoxPassSet( String memberId, ReqBoxPass boxPass );

    RspBase<RspMoney> boxAccount( String memberId, ReqBoxPass boxPass );

    RspBase<RspMoney> boxTransfer( String memberId, ReqBoxChange boxChange );

    void updateSafeBox( MemberInfo memberInfo, BigDecimal addAccount, boolean flag );

    RspBase<RspAccountMoney> getAccountNow( String memberId );

    RspBase<RspMember> getAccountInfo( String memberId );

    List<RspLogMoney> getFundDetails( String memberId, ReqLogMoney reqLogMoney );

    List<RspCodeFlow> getCodeFlowList( String memberId );

    RspVipInfo getVipGiftInfo( String memberId );

    RspBase<?> receiveVipGift( String userId, Integer type );

    void receiveVipGift( String memberId, boolean isInsert, MemberVipGift saveOrUpdate, String name, BigDecimal addMoney,
                         BigDecimal needBcode );

    RspBase<RspImToken> getImToken( String userId );

    RspBase<?> insertMemberInfo( String phone, String password );

    RspBase<?> bindPhone( MobileBind mobileBind, PlatformUser platformUser );

    RspBase<?> resetPasswd( ReqResetPasswd reqResetPasswd, PlatformUser platformUser );

    String getMemberLoginAddress( String memberId );

    RspBase<?> bindInviterCode( ReqMemberRecommend reqMemberRecommend, String userId );

    RspBase<?> insertBatchExcelMoney( String userIds );

    RspBase<?> updatePhones( ReqSmallFeatures req );

    RspBase<?> queryPhones( ReqSmallFeatures req );

    RspBase<?> commitMoney( ReqSmallFeatures req );

    RspBase<?> insertPaiSong( String req );

    RspBase<?> clear();

    void sendMsg( String msg, String memberId );

    RspBase<?> updateCodeTotalVipLevel( MemberInfo memberInfo );
}
