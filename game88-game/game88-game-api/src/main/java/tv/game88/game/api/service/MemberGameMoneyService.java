package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.entity.MemberGameMoney;

public interface MemberGameMoneyService extends IService<MemberGameMoney> {
    String selectMaxGameOrderCode( Long platformId );

    void beginGameEnter( ReqJoinGame reqJoinGame );

    void enterGameFail( ReqJoinGame reqJoinGame );

    void enterGameSuccess( ReqJoinGame reqJoinGame );

    void outGameSuccess( ReqJoinGame reqJoinGame );

    void outGameFail( ReqJoinGame reqJoinGame );
}