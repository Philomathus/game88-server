package tv.game88.core.config.constants;

/**
 * <p>Title: Constants</p>
 * <p>Description: 常量类</p>
 *
 * @author admin
 */
public class Constants {
    public static final String CONFIG_PREX   = "config:";
    public static final String MEMBER_PREX   = "member:";
    public static final String MESSAGE_PREX  = "message:";
    public static final String ACTIVITY_PREX = "activity:";
    public static final String GAME_PREX     = "game:";
    public static final String LOTTERY_PREX  = "lottery:";

    public static final String MEMBER_CODE      = MEMBER_PREX + "member-code";
    public static final Long   MEMBER_CODE_INIT = 10000L;

    /**
     * 登录会员 redis token key
     */
    public static final String MEMBER_LOGIN_TOKEN = MEMBER_PREX + "login:token:";
    /**
     * 登录会员 redis user key
     */
    public static final String MEMBER_LOGIN_USER  = MEMBER_PREX + "login:user:";
}
