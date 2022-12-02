package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.game.api.entity.MemberGameDataFix;

import java.util.List;

/**
 * 游戏注单修复Mapper接口
 *
 * @author 77tv
 * @date 2021-06-11
 */
public interface MemberGameDataFixMapper extends BaseMapper<MemberGameDataFix> {

    List<MemberGameDataFix> selectMemberGameDataFixList( MemberGameDataFix memberGameDataFix );
}
