package tv.game88.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program:
 * @description: 校验工具类
 * @author: MengJun
 * @create: 2019-01-05 16:21
 **/
public class ValidatorUtil {
    /**
     * 正则表达式：验证用户名
     */
    public static final String REGEX_USERNAME = "^[a-zA-Z]\\w{5,20}$";

    /**
     * 正则表达式：验证密码
     */
    public static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,20}$";

    /**
     * 正则表达式：验证手机号
     */
    public static final String REGEX_MOBILE = "^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{11}$";

    /**
     * 正则表达式：11位数字
     */
    public static final String NUMBER_11 = "([0-9]){11}$";

    /**
     * 正则表达式：验证邮箱
     */
    public static final String REGEX_EMAIL =
            "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2," + "}$";

    /**
     * 正则表达式：验证汉字
     */
    public static final String REGEX_CHINESE = "[\u4e00-\u9fa5]";

    /**
     * 正则表达式：验证身份证
     */
    public static final String REGEX_ID_CARD = "(^\\d{18}$)|(^\\d{15}$)";

    /**
     * 正则表达式：验证URL
     */
    public static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";

    /**
     * 正则表达式：验证IP地址
     */
    public static final String REGEX_IP_ADDR = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";

    public static final String REGEX_ACCOUNT = "^[a-zA-Z0-9]+$";

    /**
     * 校验用户名
     *
     * @param username
     *
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isUsername( String username ) {
        return Pattern.matches( REGEX_USERNAME, username );
    }

    /**
     * 校验密码
     *
     * @param password
     *
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isPassword( String password ) {
        return Pattern.matches( REGEX_PASSWORD, password );
    }

    /**
     * 校验手机号
     *
     * @param mobile
     *
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isMobile( String mobile ) {
        return Pattern.matches( REGEX_MOBILE, mobile );
    }

    /**
     * 校验是十一位数字
     *
     * @param mobile
     *
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isNumber11( String mobile ) {
        return Pattern.matches( NUMBER_11, mobile );
    }

    /**
     * 校验邮箱
     *
     * @param email
     *
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isEmail( String email ) {
        return Pattern.matches( REGEX_EMAIL, email );
    }

    /**
     * 校验汉字
     *
     * @param chinese
     *
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isChinese( String chinese ) {
        Pattern pattern = Pattern.compile( REGEX_CHINESE );
        char[]  c       = chinese.toCharArray();
        for ( char value : c ) {
            Matcher matcher = pattern.matcher( String.valueOf( value ) );
            if ( !matcher.matches() ) {
                return false;
            }
        }
        return true;
    }

    /**
     * 校验身份证
     *
     * @param idCard
     *
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isIDCard( String idCard ) {
        return Pattern.matches( REGEX_ID_CARD, idCard );
    }

    /**
     * 校验URL
     *
     * @param url
     *
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isUrl( String url ) {
        return Pattern.matches( REGEX_URL, url );
    }

    /**
     * 校验IP地址
     *
     * @param ipAddr
     */
    public static boolean isIPAddr( String ipAddr ) {
        return Pattern.matches( REGEX_IP_ADDR, ipAddr );
    }

    public static boolean isAccount( String o ) {
        return ( Pattern.compile( REGEX_ACCOUNT ) ).matcher( o ).matches();
    }

    /**
     * 校验银行卡号方法
     *
     * @param bankCard
     */
    public static boolean checkBankCard( String bankCard ) {
        if ( bankCard.length() < 15 || bankCard.length() > 19 ) {
            return false;
        }
        char bit = getBankCardCheckCode( bankCard.substring( 0, bankCard.length() - 1 ) );
        if ( bit == 'N' ) {
            return false;
        }
        return bankCard.charAt( bankCard.length() - 1 ) == bit;
    }


    /**
     * 从不含校验位的银行卡卡号采用 Luhm 校验算法获得校验位
     *
     * @param nonCheckCodeBankCard
     */
    private static char getBankCardCheckCode( String nonCheckCodeBankCard ) {
        if ( nonCheckCodeBankCard == null || nonCheckCodeBankCard.trim().isEmpty()
                || !nonCheckCodeBankCard.matches( "\\d+" ) ) {
            //如果传的不是数据返回N
            return 'N';
        }
        char[] chs     = nonCheckCodeBankCard.trim().toCharArray();
        int    luhmSum = 0;
        for ( int i = chs.length - 1, j = 0; i >= 0; i--, j++ ) {
            int k = chs[ i ] - '0';
            if ( j % 2 == 0 ) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return ( luhmSum % 10 == 0 ) ? '0' : ( char ) ( ( 10 - luhmSum % 10 ) + '0' );
    }
}
