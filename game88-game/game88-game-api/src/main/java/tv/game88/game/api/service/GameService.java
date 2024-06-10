package tv.game88.game.api.service;

import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.game.api.dto.*;

import java.util.List;

public interface GameService {
    RspGameTypes getGameTypes( String version );

    List<RspGameInfo> getGameInfoList( Long typeId );

    List<RspGameInfo> getGameInfos( Long typeId, Long platformId );

    RspBase<List<RspGamePlatform>> getGameInfoGroup( Long typeId );

    RspBase<?> joinGame( Long infoId, PlatformUser platformUser, Integer dev );

    RspBase<?> escGame( Long infoId, String memberId );

    RspBase<String> getGameTokenByAgent( String agent, String gameCategory ) throws Exception;

    RspBase<List<RspGameMoney>> getGameBalance( String memberId );

    RspBase<?> gameWithdrawal( Long platformId, String memberId );

    String verifyPGSession( ReqPGSoftGameData reqPGSoftGameData ) throws Exception;
}
