package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.dto.ReqGameDataRecord;
import tv.game88.general.api.entity.GameDataRecord;

import java.util.List;

public interface GameDataRecordService extends IService<GameDataRecord> {
    void cutTable( int num );

    void batchInsert( List<GameDataRecord> gameDataRecords );

    List<GameDataRecord> getListByReq( ReqGameDataRecord req );
}
