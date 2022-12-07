package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.game.api.entity.MemberGameMoney;

import java.util.List;

public interface MemberGameMoneyMapper extends BaseMapper<MemberGameMoney> {
    /**
     * 查询列表
     *
     * @param memberGameMoney
     *
     * @return 集合
     */
    public List<MemberGameMoney> selectMemberGameMoneyList( MemberGameMoney memberGameMoney );

    String selectMaxGameOrderCode( Long platformId );
}