package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.game.api.entity.LogCleanCodeInfo;

import java.util.List;

/**
 * Mapper接口
 *
 * @author MengJun
 */
public interface LogCleanCodeInfoMapper extends BaseMapper<LogCleanCodeInfo> {

    /**
     * 查询列表
     *
     * @param logCleanCodeInfo
     *
     * @return 集合
     */
    public List<LogCleanCodeInfo> selectLogCleanCodeInfoList( LogCleanCodeInfo logCleanCodeInfo );
}