package tv.game88.general.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.general.api.entity.GameDataRecord;

import java.util.List;

/**
 * 游戏注单数据
 *
 * @author MengJun
 */
public interface GameDataRecordMapper extends BaseMapper<GameDataRecord> {
    List<GameDataRecord> selectGameDataRecordList( GameDataRecord gameDataRecord, @Param( "tableName" ) String tableName );

    void createGameDateRecordTable( String tableName );

    void insertByTableName( @Param( "data" ) GameDataRecord gameDataRecord, @Param( "tableName" ) String tableName );

    Long findCount( @Param( "id" ) String id, @Param( "tableName" ) String tableName );
}