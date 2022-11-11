package tv.game88.game.api.service;

import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.dto.RspGameTypes;

import java.util.List;

public interface GameService {
    RspGameTypes getGameTypes();

    List<RspGameInfo> getGameInfoList( Long typeId );
}
