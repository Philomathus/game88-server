package tv.game88.general.game.base;

import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;

import java.util.List;

public interface BaseGamePull {
    List<Object> requestRemoteGameData( GamePlatform gamePlatform );

    GameDataRecord handleResult( Object remoteGameDatum, GamePlatform gamePlatform );
}
