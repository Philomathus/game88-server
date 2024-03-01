package tv.game88.general.game.dock;

import com.opencsv.CSVReader;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Repository( value = ConstantsGame.PP + "GamePullProcessor" )
public class GamePullDockPP extends AbstractGamePull {

    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        long          gamePlatformVersion = Long.parseLong( gamePlatform.getVersionValue() );
        LocalDateTime start               = LocalDateTimeUtils.getDateTimeFromTimestamp( gamePlatformVersion );
        // 如果不是5分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 5 ) ) ) {
            return null;
        }
        List<Object> resultDataList    = new ArrayList<>();
        String       firstTimeResult   = this.execute( gamePlatform, gamePlatformVersion );
        List<Object> firstTimeDataList = this.getDataList( firstTimeResult );
        if ( !CollectionUtils.isEmpty( firstTimeDataList ) ) {
            resultDataList.addAll( firstTimeDataList );
            // 加1分钟,每1分钟拉一次单
            gamePlatform.setVersionValue( String.valueOf( gamePlatformVersion + 120000 ) );
        }
        // 加10分钟再拉一次,避免漏单
        String       secondTimeResult   = this.execute( gamePlatform, gamePlatformVersion + 600000 );
        List<Object> secondTimeDataList = this.getDataList( secondTimeResult );
        if ( !CollectionUtils.isEmpty( secondTimeDataList ) ) {
            resultDataList.addAll( secondTimeDataList );
        }
        return resultDataList;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Map<String, Object> remoteGameDatum = ( Map<String, Object> ) object;
        // status I 代表正在进行中游戏,忽略  type F 代表免费旋转,无有效下注,忽略
        if ( StringUtils.equals( "I", String.valueOf( remoteGameDatum.get( "status" ) ) )
                || StringUtils.equals( "F", String.valueOf( remoteGameDatum.get( "type" ) ) ) ) {
            return null;
        }
        GameDataRecord gameDataRecord = new GameDataRecord();
        gameDataRecord.setGameId( String.valueOf( remoteGameDatum.get( "playSessionID" ) ) );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setGameRound( gameDataRecord.getGameId() );
        // 9901_M22611
        String account = String.valueOf( remoteGameDatum.get( "extPlayerID" ) );
        String agent   = account.split( "_" )[ 0 ];
        gameDataRecord.setAccount( account );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setKindId( String.valueOf( remoteGameDatum.get( "gameID" ) ) );
        gameDataRecord.setCurrency( String.valueOf( remoteGameDatum.get( "currency" ) ) );

        String bet = String.valueOf( remoteGameDatum.get( "bet" ) );
        gameDataRecord.setCellScore( bet );
        gameDataRecord.setAllBet( bet );
        BigDecimal win = new BigDecimal( String.valueOf( remoteGameDatum.get( "win" ) ) );
        gameDataRecord.setProfit( win.subtract( new BigDecimal( bet ) ).toString() );

        //gameDataRecord.setRevenue( String.valueOf( remoteGameDatum.get( "sharesScore" ) ) );
        //gameDataRecord.setTableId( String.valueOf( remoteGameDatum.get( "table" ) ) );
        //gameDataRecord.setChairId( String.valueOf( remoteGameDatum.get( "bank" ) ) );

        LocalDateTime startDate = LocalDateTimeUtils.convertUTC0ToDefault( String.valueOf( remoteGameDatum.get( "startDate" ) )
                , LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS_FORMATTER );
        gameDataRecord.setGameStartTime( LocalDateTimeUtils.format( startDate ) );
        LocalDateTime endDate = LocalDateTimeUtils.convertUTC0ToDefault( String.valueOf( remoteGameDatum.get( "endDate" ) ),
                LocalDateTimeUtils.YYYY_MM_DD_HH_MM_SS_FORMATTER );
        gameDataRecord.setGameEndTime( LocalDateTimeUtils.format( endDate ) );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }

    private List<Object> getDataList( String resultStr ) {
        //找到第一个换行符的位置
        int index = resultStr.indexOf( "\n" );
        //取出第一行数据
        String firstLine = resultStr.substring( 0, index );
        if ( StringUtils.isBlank( firstLine ) && !firstLine.contains( "=" ) ) {
            return new ArrayList<>();
        }
        //删除第一行数据
        resultStr = resultStr.substring( index + 1 );
        if ( StringUtils.isBlank( resultStr ) ) {
            return new ArrayList<>();
        }
        try {
            CSVReader csvReader = new CSVReader( new StringReader( resultStr ) );
            // 读取所有记录
            List<String[]> records = csvReader.readAll();

            // 获取 CSV 文件的头部信息
            String[] headers = records.getFirst();

            // 初始化 List<Map> 用于存储转换后的数据
            List<Object> dataList = new ArrayList<>();

            // 遍历 CSV 记录并转换为 Map
            for ( int i = 1; i < records.size(); i++ ) {
                String[]            record    = records.get( i );
                Map<String, String> recordMap = new HashMap<>();
                for ( int j = 0; j < headers.length; j++ ) {
                    recordMap.put( headers[ j ], record[ j ] );
                }
                dataList.add( recordMap );
            }
            return dataList;
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        return null;
    }

    private String execute( GamePlatform gamePlatform, long timepoint ) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add( "login", gamePlatform.getAgent() );
        params.add( "password", gamePlatform.getMd5() );
        params.add( "timepoint", timepoint + "" );
        params.add( "dataType", "RNG" );

        final String url = gamePlatform.getApiUrl() + "/IntegrationService/v3/DataFeeds/gamerounds/finished/";

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( url ).queryParams( params ).build( true );

        log.warn( uriComponents.toUriString() );

        String resultStr = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET, restTemplate.httpEntityCallback( null )
                , response -> {
            InputStream bodyStream = response.getBody();
            String      text;
            try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                text = IOUtils.toString( reader );
            }
            return text;
        } );
        return resultStr;
    }
}
