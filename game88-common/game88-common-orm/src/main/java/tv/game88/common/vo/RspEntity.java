package tv.game88.common.vo;

import lombok.Data;
import tv.game88.common.constant.HttpStatus;
import tv.game88.common.utils.StringUtils;

/**
 * <p>Title: RspBase</p>
 * <p>Description: Http operation result object </p>
 *
 * @author rajesh
 */
@Data
public class RspEntity<T> {
    private int    code;
    private String msg;
    private T      data;

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public RspEntity( int code, String msg ) {
        this.code = code;
        this.msg  = msg;
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     */
    public RspEntity( int code, String msg, T data ) {
        this.code = code;
        this.msg  = msg;
        if ( StringUtils.isNotNull( data ) ) {
            this.data = data;
        }
    }

    public static <T> RspEntity<T> ok(final String msg, T data ) {
        return new RspEntity<>( HttpStatus.SUCCESS, msg, data );
    }

    public static <T> RspEntity<T> ok(final String msg ) {
        return new RspEntity<>( HttpStatus.SUCCESS, msg );
    }

    public static <T> RspEntity<T> ok(T data ) {
        return new RspEntity<>( HttpStatus.SUCCESS, "操作成功", data );
    }

    public static <T> RspEntity<T> ok() {
        return RspEntity.ok( "操作成功" );
    }

    /**
     * 业务异常提示
     */
    public static <T> RspEntity<T> businessError(String error ) {
        return new RspEntity<>( HttpStatus.ERROR, error );
    }

    public static <T> RspEntity<T> badRequest(String error ) {
        return new RspEntity<>( HttpStatus.BAD_REQUEST, error );
    }

    /**
     * 登录异常
     */
    public static <T> RspEntity<T> sessionError(String error ) {
        return new RspEntity<>( HttpStatus.UNAUTHORIZED, error );
    }
}
