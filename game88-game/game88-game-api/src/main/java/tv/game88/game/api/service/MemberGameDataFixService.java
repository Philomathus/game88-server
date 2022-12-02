package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.game.api.entity.MemberGameDataFix;

import java.util.List;

/**
 * Service接口
 *
 * @author MengJun
 */
public interface MemberGameDataFixService extends IService<MemberGameDataFix> {
    /**
     * 查询列表
     *
     * @param memberGameDataFix
     *
     * @return 集合
     */
    public List<MemberGameDataFix> selectMemberGameDataFixList( MemberGameDataFix memberGameDataFix );
}