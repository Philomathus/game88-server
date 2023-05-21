package tv.game88.general.game.base;

import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;

import java.util.List;
import java.util.Map;

public interface BaseGamePull {
    List<Map<String, Object>> requestRemoteGameData( GamePlatform gamePlatform );

    List<GameDataRecord> handleResult( List<Map<String, Object>> dataListMap, GamePlatform gamePlatform );
}
