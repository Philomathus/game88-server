package tv.game88.game.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.common.annotation.DataSource;
import tv.game88.game.api.entity.GameDataRecord;

import java.util.List;

/**
 * 游戏注单数据
 *
 * @author MengJun
 */
public interface GameDataRecordMapper extends BaseMapper<GameDataRecord> {
    @DataSource( value = "secondaryDataSource" )
    List<GameDataRecord> selectGameDataRecordAgentList( @Param( "tableNode" ) String tableNode, @Param( "start" ) String start,
                                                        @Param( "end" ) String end, @Param( "agent" ) String agent,
                                                        @Param( "account" ) String account, @Param( "platformId" ) Long platformId );
}