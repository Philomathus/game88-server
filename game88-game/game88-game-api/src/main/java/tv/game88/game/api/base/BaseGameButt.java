package tv.game88.game.api.base;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.dto.XiaFenResult;

import java.math.BigDecimal;

public interface BaseGameButt {
    /**
     * 获取游戏token,并存储Redis
     *
     * @param reqJoinGame  进入游戏参数
     */
    @Retryable( value = Exception.class, maxAttempts = 3, backoff = @Backoff( delay = 500 ) )
    void getToken( ReqJoinGame reqJoinGame );

    /**
     * 创建游戏账号
     *
     * @param reqJoinGame  进入游戏参数
     */
    @Retryable( value = Exception.class, maxAttempts = 3, backoff = @Backoff( delay = 500 ) )
    void createAccount( ReqJoinGame reqJoinGame );

    /**
     * 获取游戏链接
     *
     * @param reqJoinGame  进入游戏参数
     *
     * @return 游戏链接地址
     */
    @Retryable( value = Exception.class, maxAttempts = 3, backoff = @Backoff( delay = 500 ) )
    String getJoinGameUrl( ReqJoinGame reqJoinGame );

    /**
     * 游戏转账
     *
     * @param reqJoinGame  进入游戏参数
     *
     * @return 是否成功转账
     */
    boolean transferMoney( ReqJoinGame reqJoinGame );

    /**
     * 游戏下分
     *
     * @param reqJoinGame  进入游戏参数
     *
     * @return 下分金额
     */
    XiaFenResult withdrawal( ReqJoinGame reqJoinGame );

    /**
     * 查询游戏内余额
     *
     * @param reqJoinGame  进入游戏参数
     *
     * @return 游戏内余额
     */
    @Retryable( value = Exception.class, maxAttempts = 3, backoff = @Backoff( delay = 500 ) )
    BigDecimal queryBalance( ReqJoinGame reqJoinGame );
}
