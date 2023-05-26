package tv.game88.general.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.general.api.dto.ReqGameDataRecord;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.mapper.GameDataRecordMapper;
import tv.game88.general.api.service.GameDataRecordService;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Service
public class GameDataRecordServiceImpl extends ServiceImpl<GameDataRecordMapper, GameDataRecord> implements GameDataRecordService {
    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String TABLE_PREFIX = "game_data_record_";

    @Override
    public void cutTable( int num ) {
        LocalDate localDate = LocalDate.now();
        for ( int i = 0; i < num; i++ ) {
            String day = LocalDateTimeUtils.format( localDate.plusDays( i ), LocalDateTimeUtils.YYYYMMDD_FORMATTER );
            this.baseMapper.createGameDateRecordTable( TABLE_PREFIX + day );
        }
    }

    @Override
    public void batchInsert( List<GameDataRecord> gameDataRecords ) {
        SqlSession           session = sqlSessionTemplate.getSqlSessionFactory().openSession( ExecutorType.BATCH, false );
        GameDataRecordMapper mapper  = session.getMapper( GameDataRecordMapper.class );
        int                  i       = 0;
        for ( GameDataRecord gameDataRecord : gameDataRecords ) {
            LocalDateTime gameEndTime = LocalDateTimeUtils.parseLocalDateTime( gameDataRecord.getGameEndTime() );
            String        day         = LocalDateTimeUtils.format( gameEndTime, LocalDateTimeUtils.YYYYMMDD_FORMATTER );
            if ( mapper.findCount( gameDataRecord.getId(), TABLE_PREFIX + day ) > 0 ) {
                continue;
            }
            mapper.insertByTableName( gameDataRecord, TABLE_PREFIX + day );
            i++;
            if ( i > 100 ) {
                session.commit();
                i = 0;
            }
        }
        if ( i > 0 ) {
            session.commit();
        }
        session.close();
    }

    @Override
    public List<GameDataRecord> getListByReq( ReqGameDataRecord req ) {
        LocalDateTime  startTime      = LocalDateTimeUtils.parseLocalDateTime( req.getStartTime() );
        String         day            = LocalDateTimeUtils.format( startTime, LocalDateTimeUtils.YYYYMMDD_FORMATTER );
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setAgent( req.getAgent() );
        gameDataRecord.setGameStartTime( req.getStartTime() );
        gameDataRecord.setGameEndTime( req.getEndTime() );
        return this.baseMapper.selectGameDataRecordList( gameDataRecord, TABLE_PREFIX + day );
    }
}
