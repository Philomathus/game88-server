package tv.game88.common.exception;

/**
 * session无效异常
 */
public class SessionExpireException extends RuntimeException {

    public SessionExpireException(String errCode) {
        super(errCode);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }

}
