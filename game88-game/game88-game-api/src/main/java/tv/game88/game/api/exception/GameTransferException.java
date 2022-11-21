package tv.game88.game.api.exception;

/**
 * 游戏转账异常
 */
public class GameTransferException extends RuntimeException {
    private final String message;

    public GameTransferException( String message ) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
