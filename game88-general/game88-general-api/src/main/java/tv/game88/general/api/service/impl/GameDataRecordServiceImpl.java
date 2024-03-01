package tv.game88.general.api.service.impl;

import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.core.game.dto.RspGameDataLog;
import tv.game88.general.api.dto.ReqGameDataRecord;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.api.mapper.GameDataRecordMapper;
import tv.game88.general.api.service.GameDataRecordService;

import jakarta.annotation.Resource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@Master
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
    public void batchInsert( List<GameDataRecord> gameDataRecords, GamePlatform gamePlatform, String name ) {
        SqlSession           session = sqlSessionTemplate.getSqlSessionFactory().openSession( ExecutorType.BATCH, false );
        GameDataRecordMapper mapper  = session.getMapper( GameDataRecordMapper.class );
        int                  i       = 0;
        int                  num     = 0;
        for ( GameDataRecord gameDataRecord : gameDataRecords ) {
            String day = LocalDateTimeUtils.format( LocalDateTime.now(), LocalDateTimeUtils.YYYYMMDD_FORMATTER );
            if ( mapper.findCount( gameDataRecord.getId(), TABLE_PREFIX + day ) > 0 ) {
                continue;
            }
            mapper.insertByTableName( gameDataRecord, TABLE_PREFIX + day );
            num++;
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

        log.info( "{}数据开始存库,预存数据条数:{};实际存储数据条数:{}", name, gameDataRecords.size(), num );
    }

    @Override
    public List<RspGameDataLog> getListByReq( ReqGameDataRecord req ) {
        LocalDateTime  startTime      = LocalDateTimeUtils.parseLocalDateTime( req.getStartTime() );
        String         day            = LocalDateTimeUtils.format( startTime, LocalDateTimeUtils.YYYYMMDD_FORMATTER );
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setAgent( req.getAgent() );
        gameDataRecord.setAccount( req.getAccount() );
        gameDataRecord.setPlatformIds( req.getPlatformIds() );
        gameDataRecord.setAgent( req.getAgent() );
        gameDataRecord.setGameStartTime( req.getStartTime() );
        gameDataRecord.setGameEndTime( req.getEndTime() );
        List<GameDataRecord> gameDataRecords = this.baseMapper.selectGameDataRecordList( gameDataRecord, TABLE_PREFIX + day );
        List<RspGameDataLog> resultList      = new ArrayList<>();
        for ( GameDataRecord dataRecord : gameDataRecords ) {
            RspGameDataLog rspGameDataLog = new RspGameDataLog();
            rspGameDataLog.setGame_id( dataRecord.getGameId() );
            rspGameDataLog.setAccount( dataRecord.getAccount() );
            rspGameDataLog.setCx_agent( dataRecord.getAgent() );
            rspGameDataLog.setAgent( dataRecord.getGameAgent() );
            rspGameDataLog.setGame_round( dataRecord.getGameRound() );
            rspGameDataLog.setId( dataRecord.getId() );
            rspGameDataLog.setAll_bet( dataRecord.getAllBet() );
            rspGameDataLog.setCell_score( dataRecord.getCellScore() );
            rspGameDataLog.setChair_id( dataRecord.getChairId() );
            rspGameDataLog.setKind_id( dataRecord.getKindId() );
            rspGameDataLog.setPlatform_id( dataRecord.getPlatformId().intValue() );
            rspGameDataLog.setTable_id( dataRecord.getTableId() );
            rspGameDataLog.setProfit( dataRecord.getProfit() );
            rspGameDataLog.setRevenue( dataRecord.getRevenue() );
            rspGameDataLog.setGame_start_time( dataRecord.getGameStartTime() );
            rspGameDataLog.setGame_end_time( dataRecord.getGameEndTime() );
            resultList.add( rspGameDataLog );
        }
        return resultList;
    }
}
