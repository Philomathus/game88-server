package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.WALI + "GamePullProcessor" )
public class GamePullDockWali extends AbstractGamePull {
    @Override
    public List<Map<String, Object>> requestRemoteGameData( GamePlatform gamePlatform ) {
        return null;
    }

    @Override
    public List<GameDataRecord> handleResult( List<Map<String, Object>> dataListMap, GamePlatform gamePlatform ) {
        return null;
    }
}
