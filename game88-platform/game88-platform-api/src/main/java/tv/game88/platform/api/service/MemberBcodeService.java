package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.entity.MemberBcode;

import java.util.List;

public interface MemberBcodeService extends IService<MemberBcode> {
    public List<MemberBcode> selectMemberBcodeList( MemberBcode memberBcode );

    public int updateMemberBcode( MemberBcode memberBcode );

    public RspBase<MemberBcode> getTotalData( MemberBcode memberBcode );
}
