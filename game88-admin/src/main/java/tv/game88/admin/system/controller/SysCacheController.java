package tv.game88.admin.system.controller;

import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import java.util.*;

/**
 * 缓存监控
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/monitor/cache" )
public class SysCacheController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PreAuthorize( "@ss.hasPermi('monitor:cache:list')" )
    @GetMapping()
    public RspBase<Map<String, Object>> getInfo() throws Exception {
        Properties info = ( Properties ) stringRedisTemplate.execute( ( RedisCallback<Object> ) RedisServerCommands::info );
        Properties commandStats =
				( Properties ) stringRedisTemplate.execute( ( RedisCallback<Object> ) connection -> connection.info(
						"commandstats" ) );
        Object dbSize = stringRedisTemplate.execute( ( RedisCallback<Object> ) RedisServerCommands::dbSize );

        Map<String, Object> result = new HashMap<>( 3 );
        result.put( "info", info );
        result.put( "dbSize", dbSize );

        List<Map<String, String>> pieList = new ArrayList<>();
        commandStats.stringPropertyNames().forEach( key -> {
            Map<String, String> data     = new HashMap<>( 2 );
            String              property = commandStats.getProperty( key );
            data.put( "name", StringUtils.removeStart( key, "cmdstat_" ) );
            data.put( "value", StringUtils.substringBetween( property, "calls=", ",usec" ) );
            pieList.add( data );
        } );
        result.put( "commandStats", pieList );
        return RspBase.ok( result );
    }
}
