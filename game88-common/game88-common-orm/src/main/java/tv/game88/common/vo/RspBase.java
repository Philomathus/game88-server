package tv.game88.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import tv.game88.common.constant.HttpStatus;
import tv.game88.common.utils.StringUtils;

/**
 * <p>Title: RspBase</p>
 * <p>Description: Http操作结果对象</p>
 *
 * @author admin
 */
@Data
@NoArgsConstructor
@Schema( title = "返回实体对象" )
public class RspBase<T> {
    @Schema( title = "业务状态码", description = "200=成功,401=登录异常,其他=业务异常")
    private int    code;
    @Schema( title = "提示信息" )
    private String msg;
    @Schema( title = "业务数据" )
    private T      data;

    @Schema( title = "总条数" )
    private Long total;

    /**
     * 是否有下一页
     */
    @Schema( title = "是否有下一页" )
    private Boolean hasNext = false;

    @Schema( title = "其它数据" )
    private String otherData;

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public RspBase( int code, String msg ) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     */
    public RspBase( int code, String msg, T data ) {
        this.code = code;
        this.msg = msg;
        if ( StringUtils.isNotNull( data ) ) {
            this.data = data;
        }
    }

    public static <T> RspBase<T> ok( final String msg, T data ) {
        return new RspBase<>( HttpStatus.SUCCESS, msg, data );
    }

    public static <T> RspBase<T> ok( final String msg ) {
        return new RspBase<>( HttpStatus.SUCCESS, msg );
    }

    public static <T> RspBase<T> ok( T data ) {
        return new RspBase<>( HttpStatus.SUCCESS, "操作成功", data );
    }

    public static <T> RspBase<T> ok() {
        return RspBase.ok( "操作成功" );
    }

    /**
     * 业务异常提示
     *
     * @return
     */
    public static <T> RspBase<T> businessError( String error ) {
        return new RspBase<>( HttpStatus.ERROR, error );
    }

    public static <T> RspBase<T> noMoneyError( String error ) {
        return new RspBase<>( HttpStatus.NO_MONEY, error );
    }

    /**
     * 登录异常
     *
     * @return
     */
    public static <T> RspBase<T> sessionError( String error ) {
        return new RspBase<>( HttpStatus.UNAUTHORIZED, error );
    }

    /**
     * 权限异常
     *
     * @return
     */
    public static <T> RspBase<T> forbiddenError( String error ) {
        return new RspBase<>( HttpStatus.FORBIDDEN, error );
    }
}
