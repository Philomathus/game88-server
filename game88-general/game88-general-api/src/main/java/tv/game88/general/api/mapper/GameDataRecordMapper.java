package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.general.api.entity.GameDataRecord;

/**
 * 游戏注单数据
 *
 * @author MengJun
 */
public interface GameDataRecordMapper extends BaseMapper<GameDataRecord> {
    void createGameDateRecordTable( String tableName );
}