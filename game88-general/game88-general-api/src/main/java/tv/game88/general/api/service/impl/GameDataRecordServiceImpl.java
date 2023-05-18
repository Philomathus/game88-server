package tv.game88.general.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.mapper.GameDataRecordMapper;
import tv.game88.general.api.service.GameDataRecordService;

import java.time.LocalDate;

@Log4j2
@Service
public class GameDataRecordServiceImpl extends ServiceImpl<GameDataRecordMapper, GameDataRecord> implements GameDataRecordService {
    @Override
    public void cutTable( int num ) {
        LocalDate localDate = LocalDate.now();
        for ( int i = 0; i < num; i++ ) {
            String day = LocalDateTimeUtils.format( localDate.plusDays( i ), LocalDateTimeUtils.YYYYMMDD_FORMATTER );
            this.baseMapper.createGameDateRecordTable( "game_data_record_" + day );
        }
    }
}
