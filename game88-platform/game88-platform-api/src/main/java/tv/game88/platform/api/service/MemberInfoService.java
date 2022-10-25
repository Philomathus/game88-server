package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.platform.api.dto.RspInit;
import tv.game88.platform.api.dto.RspManUpdateVersion;

public interface MemberInfoService extends IService<MemberInfo> {
    RspInit getLoginInit( Integer dev, String version );

    RspManUpdateVersion checkManUpdateVersion( Integer dev, String version );
}
