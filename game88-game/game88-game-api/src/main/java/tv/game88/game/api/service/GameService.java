package tv.game88.game.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.game.api.dto.RspGameInfo;
import tv.game88.game.api.dto.RspGameTypes;

import java.util.List;

public interface GameService {
    RspGameTypes getGameTypes();

    List<RspGameInfo> getGameInfoList( Long typeId );

    RspBase<?> joinGame( Long infoId, PlatformUser platformUser );

    RspBase<?> escGame( Long infoId, PlatformUser platformUser );
}
