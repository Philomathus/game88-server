package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.game.api.dto.RspGameWashCodeLog;
import tv.game88.game.api.entity.GameWashCodeLog;

import java.util.List;

/**
 * 新会员洗码记录Mapper接口
 *
 * @author mengjun
 */
public interface GameWashCodeLogMapper extends BaseMapper<GameWashCodeLog> {

    /**
     * 查询新会员洗码记录列表
     *
     * @param gameWashCodeLog 新会员洗码记录
     *
     * @return 新会员洗码记录集合
     */
    List<GameWashCodeLog> selectGameWashCodeLogList( GameWashCodeLog gameWashCodeLog );

    List<RspGameWashCodeLog> selectRspList( String memberId );
}
