package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.dto.ReqLogMoney;
import tv.game88.core.member.dto.RspCodeFlow;
import tv.game88.core.member.dto.RspLogMoney;
import tv.game88.core.member.dto.RspMember;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.platform.api.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MemberInfoService extends IService<MemberInfo> {
    RspInit getLoginInit( Integer dev, String version );

    RspManUpdateVersion checkManUpdateVersion( Integer dev, String version );

    RspBase<RspMember> login( MobileLogin mobileLogin, Integer dev, String version, String loginUrl );

    RspBase<RspMember> loginDevice( MobileLogin mobileLogin, Integer dev, String version, String loginUrl );

    RspBase<RspMember> register( MobileLogin mobileLogin, Integer dev, String version, String loginUrl );

    RspBase<?> sendSmsVerifyCode( Phone phone );

    RspBase<?> addMemberMoneyOnly( String ip, String userName, ReqAddScore req );

    List<MemberInfo> selectMemberInfoList( MemberInfo memberInfo );

    Map listCount( MemberInfo memberInfo );

    RspBase<?> updateMobile( String newMobile, String memberId );

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

    RspBase<RspMemberDetail> getAccountInfo( String memberId );

    List<RspLogMoney> getFundDetails( String memberId, ReqLogMoney reqLogMoney );

    List<RspConfigTradeType> getTradeTypes();

    List<RspCodeFlow> getCodeFlowList( String memberId );
}
