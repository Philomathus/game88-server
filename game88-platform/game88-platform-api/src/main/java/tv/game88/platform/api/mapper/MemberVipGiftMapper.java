package tv.game88.platform.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.platform.api.entity.MemberVipGift;

import java.util.List;

/**
 * Mapper接口
 *
 * @author MengJun
 */
public interface MemberVipGiftMapper extends BaseMapper<MemberVipGift> {

    /**
     * 查询列表
     *
     * @param memberVipGift
     *
     * @return 集合
     */
    public List<MemberVipGift> selectMemberVipGiftList( MemberVipGift memberVipGift );
}