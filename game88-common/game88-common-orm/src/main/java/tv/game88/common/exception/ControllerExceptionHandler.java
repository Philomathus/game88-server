package tv.game88.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.vo.RspBase;

import java.util.List;

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
    public RspBase<?> resolveException( Exception e ) throws Exception {
        if ( e instanceof SessionExpireException || e instanceof NotLoginException ) {
            return RspBase.sessionError( e.getMessage() );
        } else if ( e instanceof NumberFormatException ) {
            log.error( e.getMessage(), e );
            return new RspBase<>( 1, "数据格式校验失败" );
        } else if ( e instanceof BusinessException ) {
            log.error( e.getMessage(), e );
            return RspBase.businessError( e.getMessage() );
        } else if ( e instanceof NoMoneyException ) {
            return RspBase.noMoneyError( e.getMessage() );
        } else if ( "AccessDeniedException".equals( e.getClass().getSimpleName() ) ) {
            return RspBase.businessError( "你的用户所属角色没有操作权限" );
        } else if ( e instanceof AsyncRequestTimeoutException ) {
            throw e;
        } else if ( e instanceof NoResourceFoundException ) {
            HttpServletRequest request = ServletUtil.getHttpServletRequest();
            log.warn( "资源不存在 - url:{}, IP:{}, msg:{}, dev:{}", request
                    .getRequestURL()
                    .toString(), ServletUtil.getIp( request ), e.getMessage(), request.getHeader( "dev" ) );
            return RspBase.businessError( "资源不存在" );
        } else {
            HttpServletRequest request = ServletUtil.getHttpServletRequest();
            log.error( "异常请求url:{}, IP:{}, msg:{}, dev:{}", request
                    .getRequestURL()
                    .toString(), ServletUtil.getIp( request ), e.getMessage(), request.getHeader( "dev" ), e );
            return RspBase.businessError( "系统错误,请联系值班技术" );
        }
    }

    @ResponseStatus( HttpStatus.BAD_REQUEST )
    @ResponseBody
    @ExceptionHandler( MethodArgumentNotValidException.class )
    public RspBase<?> methodArgumentNotValidException( MethodArgumentNotValidException ex ) {
        BindingResult    result      = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        RspBase<?>       error       = new RspBase<>( HttpStatus.BAD_REQUEST.value(), "数据验证错误:" );
        for ( FieldError fieldError : fieldErrors ) {
            error.setMsg( error.getMsg().concat( fieldError.getField() + ":" + fieldError.getDefaultMessage() + ";" ) );
        }
        return error;
    }
}
