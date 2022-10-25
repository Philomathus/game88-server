package tv.game88.common.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class ServletUtil {

    /**
     * 获取当前请求request
     */
    public static HttpServletRequest getHttpServletRequest() {
        return (( ServletRequestAttributes ) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 获取当前请求session
     */
    public static HttpSession getHttpSession() {
        return getHttpServletRequest().getSession();
    }

    /**
     * 获取当前请求response * @return
     */
    public static HttpServletResponse getHttpServletResponse() {
        return (( ServletRequestAttributes ) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    /**
     * 获取String参数
     */
    public static String getParameter( String name ) {
        return getHttpServletRequest().getParameter( name );
    }

    /**
     * 获取String参数
     */
    public static String getParameter( String name, String defaultValue ) {
        return Convert.toStr( getHttpServletRequest().getParameter( name ), defaultValue );
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt( String name ) {
        return Convert.toInt( getHttpServletRequest().getParameter( name ) );
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt( String name, Integer defaultValue ) {
        return Convert.toInt( getHttpServletRequest().getParameter( name ), defaultValue );
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     */
    public static void renderString( HttpServletResponse response, String string ) {
        try {
            response.setStatus( 200 );
            response.setContentType( "application/json" );
            response.setCharacterEncoding( "utf-8" );
            response.getWriter().print( string );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * 是否是Ajax异步请求
     */
    public static boolean isAjaxRequest( HttpServletRequest request ) {
        String accept = request.getHeader( "accept" );
        if (accept != null && accept.contains( "application/json" ) ) {
            return true;
        }
        String xRequestedWith = request.getHeader( "X-Requested-With" );
        if (xRequestedWith != null && xRequestedWith.contains( "XMLHttpRequest" ) ) {
            return true;
        }
        String uri = request.getRequestURI();
        if (StringUtils.inStringIgnoreCase( uri, ".json", ".xml" )) {
            return true;
        }
        String ajax = request.getParameter( "__ajax" );
        return StringUtils.inStringIgnoreCase( ajax, "json", "xml" );
    }

    public static String getIp() {
        return getIp( getHttpServletRequest() );
    }

    /**
     * 获取客户端请求的真实ip地址
     *
     * @param request 请求对象
     *
     * @return String 客户端请求ip
     */
    public static String getIp( HttpServletRequest request ) {
        String             ip      = request.getHeader( "cf-pseudo-ipv4" );
        if ( ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase( ip ) ) {
            ip = request.getHeader( "client-ip" );
        }
        if ( ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase( ip ) ) {
            ip = request.getHeader( "cf-connecting-ip" );
        }
        if ( ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase( ip ) ) {
            ip = request.getHeader( "ali-cdn-real-ip" );
        }
        if ( ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase( ip ) ) {
            ip = request.getHeader( "x-real-ip" );
        }
        if ( ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase( ip ) ) {
            ip = request.getHeader( "x-forwarded-for" );
        }
        if ( ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase( ip ) ) {
            ip = request.getHeader( "HTTP_CLIENT_IP" );
        }
        if ( ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase( ip ) ) {
            ip = request.getHeader( "wl-proxy-client-ip" );
        }
        if (ip != null && ip.contains( "," )) {
            ip = ip.split( "," )[0];
        }
        if (org.apache.commons.lang3.StringUtils.isBlank( ip )) {
            ip = request.getRemoteAddr();
            log.warn( JsonUtil.object2Json( getRequestInfo( request ) ) );
        }
        return "0:0:0:0:0:0:0:1".equals( ip ) ? "127.0.0.1" : getMultistageReverseProxyIp( ip );
    }

    //得到所有的消息头
    private static Map<String, String> getRequestInfo( HttpServletRequest request ) {
        Enumeration<String> e         = request.getHeaderNames();
        Map<String, String> resultMap = new HashMap<>();
        while ( e.hasMoreElements() ) {
            String              headerName   = e.nextElement();//透明称
            Enumeration<String> headerValues = request.getHeaders( headerName );
            while ( headerValues.hasMoreElements() ) {
                resultMap.put( headerName, headerValues.nextElement() );
            }
        }
        return resultMap;
    }

    public static boolean internalIp( String ip ) {
        byte[] addr = textToNumericFormatV4( ip );
        return internalIp( addr ) || "127.0.0.1".equals( ip );
    }

    private static boolean internalIp( byte[] addr ) {
        if (StringUtils.isNull( addr ) || addr.length < 2) {
            return true;
        }
        final byte b0 = addr[0];
        final byte b1 = addr[1];
        // 10.x.x.x/8
        final byte SECTION_1 = 0x0A;
        // 172.16.x.x/12
        final byte SECTION_2 = ( byte ) 0xAC;
        final byte SECTION_3 = ( byte ) 0x10;
        final byte SECTION_4 = ( byte ) 0x1F;
        // 192.168.x.x/16
        final byte SECTION_5 = ( byte ) 0xC0;
        final byte SECTION_6 = ( byte ) 0xA8;
        switch ( b0 ) {
        case SECTION_1:
            return true;
        case SECTION_2:
            if (b1 >= SECTION_3 && b1 <= SECTION_4) {
                return true;
            }
        case SECTION_5:
            if (b1 == SECTION_6) {
                return true;
            }
        default:
            return false;
        }
    }

    /**
     * 将IPv4地址转换成字节
     *
     * @param text IPv4地址
     *
     * @return byte 字节
     */
    public static byte[] textToNumericFormatV4( String text ) {
        if (text.length() == 0) {
            return null;
        }
        byte[]   bytes    = new byte[4];
        String[] elements = text.split( "\\.", -1 );
        try {
            long l;
            int  i;
            switch ( elements.length ) {
            case 1:
                l = Long.parseLong( elements[0] );
                if ((l < 0L) || (l > 4294967295L)) {
                    return null;
                }
                bytes[0] = ( byte ) ( int ) (l >> 24 & 0xFF);
                bytes[1] = ( byte ) ( int ) ((l & 0xFFFFFF) >> 16 & 0xFF);
                bytes[2] = ( byte ) ( int ) ((l & 0xFFFF) >> 8 & 0xFF);
                bytes[3] = ( byte ) ( int ) (l & 0xFF);
                break;
            case 2:
                l = Integer.parseInt( elements[0] );
                if ((l < 0L) || (l > 255L)) {
                    return null;
                }
                bytes[0] = ( byte ) ( int ) (l & 0xFF);
                l = Integer.parseInt( elements[1] );
                if ((l < 0L) || (l > 16777215L)) {
                    return null;
                }
                bytes[1] = ( byte ) ( int ) (l >> 16 & 0xFF);
                bytes[2] = ( byte ) ( int ) ((l & 0xFFFF) >> 8 & 0xFF);
                bytes[3] = ( byte ) ( int ) (l & 0xFF);
                break;
            case 3:
                for ( i = 0; i < 2; ++i ) {
                    l = Integer.parseInt( elements[i] );
                    if ((l < 0L) || (l > 255L)) {
                        return null;
                    }
                    bytes[i] = ( byte ) ( int ) (l & 0xFF);
                }
                l = Integer.parseInt( elements[2] );
                if ((l < 0L) || (l > 65535L)) {
                    return null;
                }
                bytes[2] = ( byte ) ( int ) (l >> 8 & 0xFF);
                bytes[3] = ( byte ) ( int ) (l & 0xFF);
                break;
            case 4:
                for ( i = 0; i < 4; ++i ) {
                    l = Integer.parseInt( elements[i] );
                    if ((l < 0L) || (l > 255L)) {
                        return null;
                    }
                    bytes[i] = ( byte ) ( int ) (l & 0xFF);
                }
                break;
            default:
                return null;
            }
        } catch ( NumberFormatException e ) {
            return null;
        }
        return bytes;
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     *
     * @return 第一个非unknown IP地址
     */
    public static String getMultistageReverseProxyIp( String ip ) {
        // 多级反向代理检测
        if (ip != null && ip.indexOf( "," ) > 0) {
            final String[] ips = ip.trim().split( "," );
            for ( String subIp : ips ) {
                if ( !isUnknown( subIp ) ) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * 获取IP地址
     *
     * @return 本地IP地址
     */
    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch ( UnknownHostException ignored ) {
        }
        return "127.0.0.1";
    }

    /**
     * 获取主机名
     *
     * @return 本地主机名
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch ( UnknownHostException ignored ) {
        }
        return "未知";
    }

    /**
     * 检测给定字符串是否为未知，多用于检测HTTP请求相关
     *
     * @param checkString 被检测的字符串
     *
     * @return 是否未知
     */
    public static boolean isUnknown( String checkString ) {
        return StringUtils.isBlank( checkString ) || "unknown".equalsIgnoreCase( checkString );
    }
}
