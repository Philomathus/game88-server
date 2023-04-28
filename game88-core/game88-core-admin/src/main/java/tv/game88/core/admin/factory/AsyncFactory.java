package tv.game88.core.admin.factory;

import lombok.extern.log4j.Log4j2;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.utils.SpringUtils;
import tv.game88.core.admin.constant.AdminConstants;
import tv.game88.core.admin.entity.SysLogininfor;
import tv.game88.core.admin.entity.SysOperLog;
import tv.game88.core.admin.service.ISysLogininforService;
import tv.game88.core.admin.service.ISysOperLogService;
import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;
import java.util.TimerTask;

/**
 * 异步工厂（产生任务用）
 *
 * @author MengJun
 */
@Log4j2
public class AsyncFactory {
    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息
     * @param args     列表
     *
     * @return 任务task
     */
    public static TimerTask recordLogininfor( final String username, final String status, final String message) {
        HttpServletRequest request   = ServletUtil.getHttpServletRequest();
        final UserAgent    userAgent = UserAgent.parseUserAgentString( request.getHeader( "User-Agent" ) );
        final String       ip        = ServletUtil.getIp( request );
        return new TimerTask() {
            @Override
            public void run() {
                String address = "";
                // 打印信息到日志
                String s = AsyncFactory.getBlock( ip ) + address + AsyncFactory.getBlock( username )
                        + AsyncFactory.getBlock( status ) + AsyncFactory.getBlock( message ) + AsyncFactory.getBlock( userAgent );
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
                SpringUtils.getBean( ISysLogininforService.class ).insertLogininfor( logininfor );
            }
        };
    }

    /**
     * 操作日志记录
     *
     * @param operLog 操作日志信息
     *
     * @return 任务task
     */
    public static TimerTask recordOper( final SysOperLog operLog ) {
        return new TimerTask() {
            @Override
            public void run() {
                // 远程查询操作地点 AddressUtils.getRealAddressByIP( operLog.getOperIp() )
                operLog.setOperLocation( "" );
                SpringUtils.getBean( ISysOperLogService.class ).insertOperlog( operLog );
            }
        };
    }

    private static String getBlock( Object msg ) {
        if ( msg == null ) {
            msg = "";
        }
        return "[" + msg + "]";
    }
}
