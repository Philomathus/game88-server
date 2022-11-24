package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.game.api.dto.RspCleanCodeLog;
import tv.game88.game.api.entity.LogCleanCode;

import java.util.List;

/**
 * 会员洗码记录Mapper接口
 *
 * @author MengJun
 */
public interface LogCleanCodeMapper extends BaseMapper<LogCleanCode> {

    /**
     * 查询会员洗码记录列表
     *
     * @param logCleanCode 会员洗码记录
     *
     * @return 会员洗码记录集合
     */
    public List<LogCleanCode> selectLogCleanCodeList( LogCleanCode logCleanCode );

    List<RspCleanCodeLog> selectRspByMemberId( String memberId );
}