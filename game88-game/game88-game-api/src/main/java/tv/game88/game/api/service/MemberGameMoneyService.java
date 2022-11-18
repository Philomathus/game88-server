package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.game.api.dto.ReqJoinGame;
import tv.game88.game.api.entity.MemberGameMoney;

public interface MemberGameMoneyService extends IService<MemberGameMoney> {
    void beginGameEnter( ReqJoinGame reqJoinGame );
}