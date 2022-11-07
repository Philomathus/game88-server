package tv.game88.common.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.vo.RspBase;

import javax.servlet.http.HttpServletRequest;

/**
 * 控制器异常处理
 *
 * @author mengJun
 */
@ControllerAdvice
@Log4j2
@Component
public abstract class ControllerExceptionHandler {
    /**
     * 控制器异常处理入口
     *
     * @param e 异常信息
     */
    @ExceptionHandler( Throwable.class )
    @ResponseBody
    @ResponseStatus( HttpStatus.OK )
    public RspBase<?> resolveException( Exception e ) {

        if ( e instanceof SessionExpireException || e instanceof NotLoginException ) {
            return RspBase.sessionError( e.getMessage() );
        } else if ( e instanceof BindException || e instanceof NumberFormatException ) {
            return new RspBase<>( 1, "数据格式校验失败" );
        } else if ( e instanceof BusinessException ) {
            log.error( e.getMessage(), e );
            return RspBase.businessError( e.getMessage() );
        } else if ( e instanceof NoMoneyException ) {
            return RspBase.noMoneyError( e.getMessage() );
        } else if ( "AccessDeniedException".equals( e.getClass().getSimpleName() ) ) {
            return RspBase.businessError( "你的用户所属角色没有操作权限" );
        } else {
            HttpServletRequest request = ServletUtil.getHttpServletRequest();
            log.error( "异常请求url:{}, IP:{}, msg:{}, dev:{}", request.getRequestURL().toString(),
                    ServletUtil.getIp( request ), e.getMessage(), request.getHeader( "dev" ), e );
            return RspBase.businessError( "系统错误,请联系值班技术" );
        }

    }
}
