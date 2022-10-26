package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.dto.RspMember;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.platform.api.dto.MobileLogin;
import tv.game88.platform.api.dto.RspInit;
import tv.game88.platform.api.dto.RspManUpdateVersion;

public interface MemberInfoService extends IService<MemberInfo> {
    RspInit getLoginInit( Integer dev, String version );

    RspManUpdateVersion checkManUpdateVersion( Integer dev, String version );

    RspBase<RspMember> login( MobileLogin mobileLogin );

    RspBase<RspMember> loginDevice( MobileLogin mobileLogin, Integer dev, String version, String loginUrl );

    RspBase<RspMember> register( MobileLogin mobileLogin );
}
