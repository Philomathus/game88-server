package tv.game88.wallet.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import tv.game88.common.constant.HttpStatus;
import tv.game88.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: RspPayResult</p>
 * <p>Description: Http操作结果对象</p>
 *
 * @author admin
 */
@Data
@NoArgsConstructor
@Schema( title = "返回Pay实体对象" )
public class RspPayResult {
    @Schema( title = "业务状态码", description = "200=成功,500=业务异常" )
    private int                       code;
    @Schema( title = "提示信息" )
    private String                    msg;
    @Schema( title = "业务数据" )
    private Object                    result;
    @Schema( title = "错误信息" )
    private List<Map<String, ?>> error = new ArrayList<>();

    /**
     * 初始化一个新创建的对象
     *
     * @param code 状态码
     * @param msg  返回内容
     */
    public RspPayResult( int code, String msg ) {
        this.code = code;
        this.msg  = msg;
    }

    /**
     * 初始化一个新创建的对象
     *
     * @param code   状态码
     * @param msg    返回内容
     * @param result 数据对象
     */
    public RspPayResult( int code, String msg, Object result ) {
        this.code = code;
        this.msg  = msg;
        if ( StringUtils.isNotNull( result ) ) {
            this.result = result;
        }
    }

    public static RspPayResult ok( final String msg, Object data ) {
        return new RspPayResult( HttpStatus.SUCCESS, msg, data );
    }

    public static RspPayResult ok( Object data ) {
        return new RspPayResult( HttpStatus.SUCCESS, "订单生成成功", data );
    }

    /**
     * 业务异常提示
     */
    public static RspPayResult businessError( String error ) {
        return new RspPayResult( HttpStatus.ERROR, error );
    }

}
