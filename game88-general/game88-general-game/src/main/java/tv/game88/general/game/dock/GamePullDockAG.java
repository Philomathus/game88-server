package tv.game88.general.game.dock;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.constants.ConstantsGame;
import tv.game88.general.api.entity.GameDataRecord;
import tv.game88.general.api.entity.GamePlatform;
import tv.game88.general.game.base.AbstractGamePull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Log4j2
@Repository( value = ConstantsGame.AG + "GamePullProcessor" )
public class GamePullDockAG extends AbstractGamePull {
    @Override
    public List<Object> requestRemoteGameData( GamePlatform gamePlatform ) {
        LocalDateTime start = LocalDateTimeUtils.getDateTimeFromTimestamp( Long.parseLong( gamePlatform.getVersionValue() ) );
        // 如果不是3分钟前的时间,跳过
        if ( start.isAfter( LocalDateTime.now().minusMinutes( 3 ) ) ) {
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

    private List<Object> queryList( GamePlatform gamePlatform, String queryXml, LocalDateTime startMD,
                                                 LocalDateTime endMD ) {
        return null;
    }

    @Override
    public GameDataRecord handleResult( Object object, GamePlatform gamePlatform ) {

        return null;
    }

    public static String convertNum( String data ) {
        data = data.equals( "" ) ? "0" : data;
        return new BigDecimal( StringUtils.isBlank( data ) ? "0" : data ).setScale( 2, RoundingMode.HALF_UP ).toString();
    }
}
