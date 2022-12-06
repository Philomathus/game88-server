package tv.game88.game.api.base;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import tv.game88.common.exception.BusinessException;
import tv.game88.game.api.dto.ReqJoinGame;

import java.math.BigDecimal;

public interface BaseGameButt {
    /**
     * 获取游戏token,并存储Redis
     *
     * @param reqJoinGame 游戏参数
     */
    @Retryable( value = Exception.class, exclude = BusinessException.class, maxAttempts = 3, backoff = @Backoff( delay = 500 ) )
    void getToken( ReqJoinGame reqJoinGame );

    /**
     * 创建游戏账号
     *
     * @param reqJoinGame 游戏参数
     */
    @Retryable( value = Exception.class, exclude = BusinessException.class, maxAttempts = 3, backoff = @Backoff( delay = 500 ) )
    void createAccount( ReqJoinGame reqJoinGame );

    /**
     * 获取游戏链接
     *
     * @param reqJoinGame 游戏参数
     *
     * @return 游戏链接地址
     */
    @Retryable( value = Exception.class, exclude = BusinessException.class, maxAttempts = 3, backoff = @Backoff( delay = 500 ) )
    void getJoinGameUrl( ReqJoinGame reqJoinGame );

    /**
     * 游戏转账
     *
     * @param reqJoinGame 游戏参数
     */
    void transferMoney( ReqJoinGame reqJoinGame );

    /**
     * 游戏下分
     *
     * @param reqJoinGame 游戏参数
     *
     * @return 下分金额
     */
    void withdrawal( ReqJoinGame reqJoinGame );

    /**
     * 查询游戏内余额
     *
     * @param reqJoinGame 游戏参数
     *
     * @return 游戏内余额
     */
    @Retryable( value = Exception.class, exclude = BusinessException.class, maxAttempts = 3, backoff = @Backoff( delay = 500 ) )
    BigDecimal queryBalance( ReqJoinGame reqJoinGame );

    /**
     * 查询游戏转账记录
     *
     * @param reqJoinGame 游戏参数
     *
     * @return 是否存在转账记录
     */
    @Retryable( value = Exception.class, exclude = BusinessException.class, maxAttempts = 5, backoff = @Backoff( delay = 500 ) )
    boolean queryTransfer( ReqJoinGame reqJoinGame );

    //List<Map<String, Object>> queryBetRecord();
}
