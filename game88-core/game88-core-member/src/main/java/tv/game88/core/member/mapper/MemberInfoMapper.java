package tv.game88.core.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.core.member.dto.RspMember;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.vo.PlatformUser;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MemberInfoMapper extends BaseMapper<MemberInfo> {
    /**
     * 查询会员表列表
     *
     * @param memberInfo 会员表
     *
     * @return 会员表集合
     */
    public List<MemberInfo> selectMemberInfoList( MemberInfo memberInfo );

    void deleteByHistoryKey( @Param( "userId" ) String userId );

    String selectMaxMemberCode();

    MemberInfo findMemberByDeviceId( @Param( "deviceId" ) String deviceId );

    MemberInfo findMemberHistoryByDeviceId( @Param( "deviceId" ) String deviceId );

    MemberInfo findMemberHistoryByMobile( String mobile );

    MemberInfo findMemberByMobile( String mobile );

    RspMember findMemberDetail( @Param( "userId" ) String userId );

    int reduceMoney( @Param( "userId" ) String pUserId, @Param( "money" ) BigDecimal money );

    /**
     * 获取用户余额
     *
     * @param pUserId
     */
    BigDecimal getUserBalance( @Param( "userId" ) String pUserId );

    /**
     * @param userId   会员ID
     * @param money    增加的金额
     * @param charge   充值金额累加
     * @param codeMult 打码金额累加
     */
    int addMoneySelect( @Param( "userId" ) String userId, @Param( "money" ) BigDecimal money,
                        @Param( "charge" ) BigDecimal charge, @Param( "code_mult" ) BigDecimal codeMult );

    int updateVipById( MemberInfo memberInfo );

    int updateMoneySelect( @Param( "userId" ) String userId, @Param( "money" ) BigDecimal money,
                           @Param( "level_integral" ) BigDecimal level_integral,
                           @Param( "code_account" ) BigDecimal code_account, @Param( "code_total" ) BigDecimal code_total );

    //-------------------------app-------------------------------

    int subMoney( @Param( "userId" ) String userId, @Param( "money" ) BigDecimal money );

    int updateSafeBox( @Param( "userId" ) String memberId, @Param( "addBox" ) BigDecimal addBox );

    int updateBeatCode( @Param( "userId" ) String userId, @Param( "code_now" ) BigDecimal code_now,
                        @Param( "code_total" ) BigDecimal code_total );

    Map listCount( MemberInfo req );

    MemberInfo findRecommendByInviterCode( String inviterCode );

    PlatformUser selectPlatformUserByUserId( @Param( "userId" ) String userId );

    String funGetaddressProvinces( String ip );

    BigDecimal selectMemberInfoHistoryRechargeById( String memberId );

    int boxDish( String memberId );

    BigDecimal personalRecharge( @Param( "startTime" ) String startTime, @Param( "endTime" ) String endTime,
                                 @Param( "memberId" ) String memberId );

    BigDecimal personalOnlineRecharge( @Param( "startTime" ) String startTime, @Param( "endTime" ) String endTime, @Param(
            "memberId" ) String memberId );

//    BigDecimal personalAgentRecharge( @Param( "startTime" ) String startTime, @Param( "endTime" ) String endTime, @Param(
//            "memberId" ) String memberId );

    BigDecimal personalUsdtRecharge( @Param( "startTime" ) String startTime, @Param( "endTime" ) String endTime, @Param(
            "memberId" ) String memberId );

//    BigDecimal personalWithdrawRecharge( @Param( "startTime" ) String startTime, @Param( "endTime" ) String endTime, @Param(
//            "memberId" ) String memberId );

    HashMap totalAccount( @Param( "startTime" ) String startTime, @Param( "endTime" ) String endTime,
                          @Param( "memberId" ) String memberId );

//    List<Map> personalGameData( @Param( "startTime" ) String startTime, @Param( "endTime" ) String endTime,
//                                @Param( "memberId" ) String memberId, @Param( "dbNodes" ) String dbNodes );

    String selectMemberProvince( String memberId );
}
