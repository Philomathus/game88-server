package tv.game88.game.api.service;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import tv.game88.common.vo.RspBase;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.game.api.dto.*;

import java.util.List;

public interface GameService {
    RspGameTypes getGameTypes( String version );

    List<RspGameInfo> getGameInfoList( Long typeId );

    List<RspGameInfo> getGameInfos( Long typeId, Long platformId );

    RspBase<List<RspGamePlatform>> getGameInfoGroup( Long typeId );

    RspBase<?> joinGame( Long infoId, PlatformUser platformUser );

    RspBase<?> escGame( Long infoId, String memberId );

    RspBase<String> getGameTokenByAgent( String agent, String gameCategory );

    RspBase<List<RspGameMoney>> getGameBalance( String memberId );

    RspBase<?> gameWithdrawal( Long platformId, String memberId );

    @Retryable( value = Exception.class, maxAttempts = 5, backoff = @Backoff( delay = 500 ) )
    List<RspGameDataLog> remoteDataGrab( String start, String end, String account, List<Integer> platformIds );
}
