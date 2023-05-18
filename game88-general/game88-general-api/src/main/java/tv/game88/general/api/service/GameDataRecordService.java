package tv.game88.general.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.general.api.entity.GameDataRecord;

public interface GameDataRecordService extends IService<GameDataRecord> {
    void cutTable( int num );
}
