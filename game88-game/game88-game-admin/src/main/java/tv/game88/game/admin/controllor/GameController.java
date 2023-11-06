package tv.game88.game.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.game.api.dto.RspGameMoney;
import tv.game88.game.api.service.GameService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 游戏处理Controller
 *
 * @author mengJun
 * @date 2021-01-27
 */
@RestController
@RequestMapping( "/game/base" )
public class GameController extends BaseController {
    @Resource
    private GameService gameService;

    /**
     * 查询用户游戏余额
     */
    @PreAuthorize( "@ss.hasPermi('game:base:balance')" )
    @GetMapping( "/balance/{userId}" )
    public RspBase<List<RspGameMoney>> balance( @PathVariable String userId ) {
        return gameService.getGameBalance( userId );
    }

    /**
     * 游戏人工下分
     */
    @PreAuthorize( "@ss.hasPermi('game:base:esc')" )
    @Log( title = "游戏人工下分", businessType = BusinessType.AUDIT )
    @GetMapping( "/esc/{platformId}/{userId}" )
    public RspBase<?> esc( @PathVariable String userId, @PathVariable Long platformId ) {
        return gameService.gameWithdrawal( platformId, userId );
    }
}
