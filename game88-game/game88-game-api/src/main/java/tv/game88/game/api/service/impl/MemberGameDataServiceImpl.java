package tv.game88.game.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.game.api.entity.MemberGameData;
import tv.game88.game.api.mapper.MemberGameDataMapper;
import tv.game88.game.api.service.MemberGameDataService;

import java.util.List;

/**
 * 会员游戏注单数据Service业务层处理
 *
 * @author mengJun
 */
@Service
public class MemberGameDataServiceImpl extends ServiceImpl<MemberGameDataMapper, MemberGameData> implements MemberGameDataService {
    /**
     * 查询会员游戏注单数据列表
     *
     * @param memberGameData 会员游戏注单数据
     *
     * @return 会员游戏注单数据
     */
    @Override
    public List<MemberGameData> selectMemberGameDataList( MemberGameData memberGameData ) {
        return this.baseMapper.selectMemberGameDataList( memberGameData );
    }
}