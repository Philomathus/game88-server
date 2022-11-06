package tv.game88.admin.system.controller;

import lombok.extern.log4j.Log4j2;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.PageUtil;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.constant.AdminConstants;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.security.service.SysUserTokenService;
import tv.game88.core.admin.vo.LoginUser;
import tv.game88.common.base.BaseController;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 在线用户监控
 *
 * @author MengJun
 */
@Log4j2
@RestController
@RequestMapping( "/monitor/online" )
public class SysUserOnlineController extends BaseController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SysUserTokenService sysUserTokenService;

    @PreAuthorize( "@ss.hasPermi('monitor:online:list')" )
    @GetMapping( "/list" )
    public RspBase<List<LoginUser>> list( String userName ) {
        Set<LoginUser> loginUserList = stringRedisTemplate.execute( ( RedisCallback<Set<LoginUser>> ) connection -> {
            Set<LoginUser> binaryKeys = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan( ScanOptions.scanOptions().match( AdminConstants.SYS_LOGIN_TOKEN + "*" )
                                                                .count( 100 ).build() );
            while ( cursor.hasNext() ) {
                Map<byte[], byte[]> map = connection.hashCommands().hGetAll( cursor.next() );
                Map<String, String> stringMap = map.entrySet().stream()
                                                   .collect( Collectors.toMap( k -> new String( k.getKey() ),
                                                           v -> new String( v.getValue() ) ) );
                binaryKeys.add( JsonUtil.map2Object( stringMap, LoginUser.class ) );
            }
            return binaryKeys;
        } );
        ArrayList<LoginUser> userOnlineList = new ArrayList<>();
        if (!CollectionUtils.isEmpty( loginUserList )) {
            loginUserList.forEach( user -> {
                user.setUserStr( null );
                if (StringUtils.isNotBlank( userName )) {
                    if (StringUtils.equals( userName, user.getUsername() )) {
                        userOnlineList.add( user );
                    }
                } else {
                    userOnlineList.add( user );
                }
            } );
        }
        userOnlineList.sort( ( o1, o2 ) -> o2.getLoginTime().compareTo( o1.getLoginTime() ) );
        String pageNum  = ServletUtil.getParameter( "pageNum" );
        String pageSize = ServletUtil.getParameter( "pageSize" );

        return getRspBasePage( PageUtil.pageBySubList( userOnlineList, Integer.parseInt( pageSize ),
                Integer.parseInt( pageNum ) ) );
    }

    /**
     * 强退用户
     */
    @PreAuthorize( "@ss.hasPermi('monitor:online:forceLogout')" )
    @Log( title = "强退用户", businessType = BusinessType.FORCE )
    @DeleteMapping( "/{userId}" )
    public RspBase<?> forceLogout( @PathVariable Long userId ) {
        sysUserTokenService.delToken( userId );
        return RspBase.ok();
    }
}
