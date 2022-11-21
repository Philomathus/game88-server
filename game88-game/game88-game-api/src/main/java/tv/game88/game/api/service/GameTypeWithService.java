package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.game.api.entity.GameInfo;
import tv.game88.game.api.entity.GameTypeWith;

import java.util.List;

public interface GameTypeWithService extends IService<GameTypeWith> {
    List<GameTypeWith> selectGameTypeWithList( Long typeId );

    List<GameInfo> selectListNotType( Long typeId, String name );

    RspBase<?> insertTypeWith( Long typeId, List<Long> gameInfoIds );

    RspBase<?> deleteTypeWith( Long typeId, List<Long> gameInfoIds );

    RspBase<?> updateTypeWith( GameTypeWith gameTypeWith );
}
