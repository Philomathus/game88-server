package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.game.api.entity.MemberGameData;

import java.util.List;

/**
 * 会员游戏注单数据Service接口
 *
 * @author mengJun
 */
public interface MemberGameDataService extends IService<MemberGameData> {
    /**
     * 查询会员游戏注单数据列表
     *
     * @param memberGameData 会员游戏注单数据
     *
     * @return 会员游戏注单数据集合
     */
    public List<MemberGameData> selectMemberGameDataList( MemberGameData memberGameData );
}