package tv.game88.core.admin.factory;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tv.game88.common.utils.ServletUtil;
import tv.game88.core.admin.constant.AdminConstants;
import tv.game88.core.admin.entity.SysLogininfor;
import tv.game88.core.admin.entity.SysOperLog;
import tv.game88.core.admin.mapper.SysLogininforMapper;
import tv.game88.core.admin.mapper.SysOperLogMapper;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 异步工厂（产生任务用）
 *
 * @author MengJun
 */
@Log4j2
@Component
public class AsyncFactory {
    public static AsyncFactory me;

    @Resource
    private ScheduledExecutorService scheduledExecutorService;

    @Resource
    private SysLogininforMapper sysLogininforMapper;
    @Resource
    private SysOperLogMapper    sysOperLogMapper;

    @PostConstruct
    void init() {
        me = this;
    }

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息
     *
     * @return 任务task
     */
    public void recordLogininfor( final String username, final String status, final String message ) {
        HttpServletRequest request   = ServletUtil.getHttpServletRequest();
        final UserAgent    userAgent = UserAgent.parseUserAgentString( request.getHeader( "User-Agent" ) );
        final String       ip        = ServletUtil.getIp( request );

        scheduledExecutorService.schedule( () -> {
            String address = "";
            // 打印信息到日志
            String s = AsyncFactory.getBlock( ip ) + address + AsyncFactory.getBlock( username ) + AsyncFactory.getBlock( status )
                    + AsyncFactory.getBlock( message ) + AsyncFactory.getBlock( userAgent );
            log.info( s );
            // 获取客户端操作系统
            String os = userAgent.getOperatingSystem().getName();
            // 获取客户端浏览器
            String browser = userAgent.getBrowser().getName();
            // 封装对象
            SysLogininfor logininfor = new SysLogininfor();
            logininfor.setUserName( username );
            logininfor.setIpaddr( ip );
            //logininfor.setLoginLocation( address );
            logininfor.setBrowser( browser );
            logininfor.setOs( os );
            logininfor.setMsg( message );
            // 日志状态
            if ( AdminConstants.LOGIN_SUCCESS.equals( status ) || AdminConstants.LOGOUT.equals( status ) ) {
                logininfor.setStatus( AdminConstants.SUCCESS );
            } else if ( AdminConstants.LOGIN_FAIL.equals( status ) ) {
                logininfor.setStatus( AdminConstants.FAIL );
            }
            // 插入数据
            sysLogininforMapper.insert( logininfor );
        }, 10, TimeUnit.MILLISECONDS );
    }

    /**
     * 操作日志记录
     *
     * @param operLog 操作日志信息
     *
     * @return 任务task
     */
    public void recordOper( final SysOperLog operLog ) {
        scheduledExecutorService.schedule( () -> {
            // 远程查询操作地点 AddressUtils.getRealAddressByIP( operLog.getOperIp() )
            operLog.setOperLocation( "" );
            sysOperLogMapper.insert( operLog );
        }, 10, TimeUnit.MILLISECONDS );
    }

    private static String getBlock( Object msg ) {
        if ( msg == null ) {
            msg = "";
        }
        return "[" + msg + "]";
    }
}
