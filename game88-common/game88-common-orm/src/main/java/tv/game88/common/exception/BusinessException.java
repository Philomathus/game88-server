package tv.game88.common.exception;

import java.io.Serial;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;

    private final String message;

    public BusinessException( String message ) {
        this.message = message;
    }

    public BusinessException( String message, Integer code ) {
        this.message = message;
        this.code    = code;
    }

    public BusinessException( String message, Throwable e ) {
        super( message, e );
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

}
