package tv.game88.core.admin.constant;

/**
 * 通用常量信息
 *
 * @author MengJun
 */
public class AdminConstants {
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 登录用户 redis token key
     */
    public static final String SYS_LOGIN_TOKEN = "sys:login:token:";
    /**
     * 登录用户 redis user key
     */
    public static final String SYS_LOGIN_USER  = "sys:login:user:";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    public static final String USER_KEY = "userKey";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys:dictCache";
}
