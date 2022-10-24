package tv.game88.common.exception;

/**
 * 业务异常
 */
public class NoMoneyException extends RuntimeException {

    public NoMoneyException(String errCode) {
        super(errCode);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }

}
