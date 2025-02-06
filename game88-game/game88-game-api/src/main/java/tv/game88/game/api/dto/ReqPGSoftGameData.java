package tv.game88.game.api.dto;

import lombok.Data;
import org.springframework.util.MultiValueMap;

@Data
public class ReqPGSoftGameData {
    private String traceId;
    private String operatorToken;
    private String secretKey;
    private String operatorPlayerSession;
    private String ip;
    private String customParameter;
    private String gameId;
    private String betType;

    public static ReqPGSoftGameData fromQueryString( MultiValueMap<String, String> params ) {
        ReqPGSoftGameData requestObject = new ReqPGSoftGameData();
        requestObject.setTraceId( params.getFirst( "trace_id" ) );
        requestObject.setOperatorToken( params.getFirst( "operator_token" ) );
        requestObject.setSecretKey( params.getFirst( "secret_key" ) );
        requestObject.setOperatorPlayerSession( params.getFirst( "operator_player_session" ) );
        requestObject.setIp( params.getFirst( "ip" ) );
        requestObject.setCustomParameter( params.getFirst( "custom_parameter" ) );
        requestObject.setGameId( params.getFirst( "game_id" ) );
        requestObject.setBetType( params.getFirst( "bet_type" ) );

        return requestObject;
    }
}
