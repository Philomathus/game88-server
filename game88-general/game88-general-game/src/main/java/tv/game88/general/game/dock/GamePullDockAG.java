package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.utils.XmlUtil;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
@Repository ( value = ConstantsGame.AG + "GamePullProcessor" )
public class GamePullDockAG extends AbstractGamePull {
    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 4 ) ) ) {
            return null;
        }
        LocalDateTime end = start.plusMinutes( 1 );

        LocalDateTime startMD = LocalDateTimeUtils.convertToMeiDong( start );
        LocalDateTime endMD   = LocalDateTimeUtils.convertToMeiDong( end );

        List<Callable<List<Object>>> forkJoinTasks = new ArrayList<>();
        // 获取电子游戏订单数据
        forkJoinTasks.add( () -> this.queryList( gamePlatform, "getslotorders_ex.xml", startMD, endMD ) );
        // 获取捕鱼场景订单数据
        forkJoinTasks.add( () -> this.queryList( gamePlatform, "gethunterscene.xml", startMD, endMD ) );
        // 获取AG Sport订单数据
        forkJoinTasks.add( () -> this.queryList( gamePlatform, "getagsportorders_ex.xml", startMD, endMD ) );
        // 获取YoPlay订单数据
        forkJoinTasks.add( () -> this.queryList( gamePlatform, "getyoplayorders_ex.xml", startMD, endMD ) );
        // 获取视讯游戏订单数据
        forkJoinTasks.add( () -> this.queryList( gamePlatform, "getorders.xml", startMD, endMD ) );

        List<Future<List<Object>>> futures = forkJoinPool.invokeAll( forkJoinTasks );
        List<List<Object>> collect = futures.stream().map( t -> {
            try {
                return t.get();
            } catch ( InterruptedException | ExecutionException e ) {
                throw new IllegalStateException( e );
            }
        } ).filter( Objects::nonNull ).toList();
        List<Object> resultList = new ArrayList<>();
        for ( List<Object> mapList : collect ) {
            resultList.addAll( mapList );
        }
        // 状态正常,无论是否有数据,从结束时间开始查询
        gamePlatform.setVersionValue( String.valueOf( LocalDateTimeUtils.localDateToTimestamp( end ) ) );
        return resultList;
    }

    private List<Object> queryList( GamePlatform gamePlatform, String queryXml, LocalDateTime startMD, LocalDateTime endMD ) {
        String startTime;
        String endTime;
        if ( "gethunterscene.xml".equals( queryXml ) ) {
            startTime = String.valueOf( LocalDateTimeUtils.localDateToTimestamp( startMD ) / 1000L );
            endTime = String.valueOf( LocalDateTimeUtils.localDateToTimestamp( endMD ) / 1000L );
        } else {
            startTime = LocalDateTimeUtils.format( startMD );
            endTime = LocalDateTimeUtils.format( endMD );
        }

        String by      = "DESC";
        String page    = "1";
        String perpage = "500";
        String order   = "";

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.set( "cagent", gamePlatform.getLinecode() );
        requestMap.set( "startdate", startTime );
        requestMap.set( "enddate", endTime );
        if ( "gethunterscene.xml".equals( queryXml ) ) {
            order = "billtime";
            requestMap.set( "order", order );
        }
        requestMap.set( "by", by );
        requestMap.set( "page", page );
        requestMap.set( "perpage", perpage );
        requestMap.set( "key", DigestUtils.md5Hex( gamePlatform.getLinecode() + startTime + endTime + order + by + page + perpage
                + "5F14237EE2A67EF102203A4C97603BC5" ) );

        UriComponents uriComponents = UriComponentsBuilder.fromUriString( gamePlatform.getRecordUrl() + queryXml )
                .queryParams( requestMap ).build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType( MediaType.APPLICATION_JSON );
        httpHeaders.add( "User-Agent", "WEB_LIB_GI_GY9_AGIN" );
        HttpEntity<?> httpEntity = new HttpEntity<>( httpHeaders );

        String resultXml = restTemplate.execute( uriComponents.toUri(), HttpMethod.GET,
                restTemplate.httpEntityCallback( httpEntity ), response -> {
                    InputStream bodyStream = response.getBody();
                    String      text;
                    try ( Reader reader = new InputStreamReader( bodyStream ) ) {
                        text = IOUtils.toString( reader );
                    }
                    return text;
                } );
        if ( resultXml.length() < 1000 ) {
            log.warn( queryXml + ":::" + uriComponents.toUriString() + ":::" + resultXml );
        }
        if ( StringUtils.isNotBlank( resultXml ) ) {
            try {
                Document document = XmlUtil.getDocument( resultXml );
                Element  root     = document.getDocumentElement(); // 获取根元素
                NodeList nodeList = root.getElementsByTagName( "row" );
                return IntStream.range( 0, nodeList.getLength() ).mapToObj( nodeList::item ).collect( Collectors.toList() );
            } catch ( Exception e ) {
                log.error( e.getMessage(), e );
                throw new BusinessException( e.getMessage() );
            }
        }
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {
        Element        element        = ( Element ) object;
        GameDataRecord gameDataRecord = new GameDataRecord();

        String reckontime = element.getAttribute( "reckontime" );
        if ( StringUtils.isBlank( reckontime ) ) {
            reckontime = element.getAttribute( "betTime" );
        }
        String reckonTime;
        if ( StringUtils.isBlank( reckontime ) ) {
            String        billtime = element.getAttribute( "billtime" );
            LocalDateTime meiDong;
            if ( StringUtils.isNumeric( billtime ) ) {
                LocalDateTime toMeiDong = LocalDateTimeUtils.convertTimestampToMeiDong( Long.parseLong( billtime + "000" ) );
                meiDong = LocalDateTimeUtils.convertMeiDongToDefault( LocalDateTimeUtils.format( toMeiDong ) );
            } else {
                meiDong = LocalDateTimeUtils.convertMeiDongToDefault( billtime );
            }
            reckonTime = LocalDateTimeUtils.format( meiDong );
        } else {
            reckonTime = LocalDateTimeUtils.format( LocalDateTimeUtils.convertMeiDongToDefault( reckontime ) );
        }
        String account = element.getAttribute( "username" );
        if ( StringUtils.isBlank( account ) ) {
            account = element.getAttribute( "playName" );
        }
        String agent = account.split( "_" )[ 0 ].toLowerCase();

        gameDataRecord.setGameId( element.getAttribute( "billno" ) );
        String gameCode = element.getAttribute( "gameCode" );
        if ( StringUtils.isBlank( gameCode ) ) {
            gameCode = element.getAttribute( "gmcode" );
        }
        gameDataRecord.setGameRound( gameCode );
        gameDataRecord.setId( this.createRecordId( gamePlatform, gameDataRecord.getGameId() ) );
        gameDataRecord.setAccount( account.toLowerCase() );

        String gametype = element.getAttribute( "gametype" );
        if ( StringUtils.isBlank( gametype ) ) {
            gametype = element.getAttribute( "gameType" );
        }
        gameDataRecord.setKindId( gametype );

        String cellScore = element.getAttribute( "valid_account" );
        if ( StringUtils.isBlank( cellScore ) ) {
            cellScore = element.getAttribute( "validBetAmount" );
        }
        gameDataRecord.setCellScore( this.convertNum( cellScore ) );

        String allBet = element.getAttribute( "account" );
        if ( StringUtils.isBlank( allBet ) ) {
            allBet = element.getAttribute( "betAmount" );
        }
        gameDataRecord.setAllBet( this.convertNum( allBet ) );

        String cusAccount = element.getAttribute( "cus_account" );
        if ( StringUtils.isBlank( cusAccount ) ) {
            cusAccount = element.getAttribute( "netAmount" );
        }
        gameDataRecord.setProfit( this.convertNum( cusAccount ) );
        gameDataRecord.setTableId( element.getAttribute( "tableCode" ) );
        gameDataRecord.setGameStartTime( reckonTime );
        gameDataRecord.setGameEndTime( reckonTime );
        gameDataRecord.setAgent( agent );
        gameDataRecord.setGameAgent( gamePlatform.getAgent() );
        gameDataRecord.setPlatformId( gamePlatform.getId() );
        return gameDataRecord;
    }

    public String convertNum( String data ) {
        data = data.equals( "" ) ? "0" : data;
        return new BigDecimal( StringUtils.isBlank( data ) ? "0" : data ).setScale( 2, RoundingMode.HALF_UP ).toString();
    }
}
