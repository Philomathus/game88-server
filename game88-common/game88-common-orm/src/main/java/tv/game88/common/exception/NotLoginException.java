package tv.game88.common.exception;

public class NotLoginException extends RuntimeException {
    public NotLoginException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
