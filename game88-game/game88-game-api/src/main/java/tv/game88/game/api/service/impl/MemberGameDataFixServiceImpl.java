package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.game.api.entity.MemberGameDataFix;
import tv.game88.game.api.mapper.MemberGameDataFixMapper;
import tv.game88.game.api.service.MemberGameDataFixService;

import java.util.List;

/**
 * Service业务层处理
 *
 * @author MengJun
 */
@Service
public class MemberGameDataFixServiceImpl extends ServiceImpl<MemberGameDataFixMapper, MemberGameDataFix> implements MemberGameDataFixService {
    /**
     * 查询列表
     *
     * @param memberGameDataFix
     */
    @Override
    public List<MemberGameDataFix> selectMemberGameDataFixList( MemberGameDataFix memberGameDataFix ) {
        return this.baseMapper.selectMemberGameDataFixList( memberGameDataFix );
    }
}