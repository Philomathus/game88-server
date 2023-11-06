package tv.game88.game.admin.controllor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.game.api.entity.MemberGameMoney;
import tv.game88.game.api.service.MemberGameMoneyService;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * Controller
 *
 * @author MengJun
 */
@RestController
@RequestMapping( "/game/memberGameMoney" )
public class MemberGameMoneyController extends BaseController {
    @Resource
    private MemberGameMoneyService memberGameMoneyService;

    /**
     * 查询列表
     */
    @PreAuthorize( "@ss.hasPermi('game:memberGameMoney:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberGameMoney>> list( MemberGameMoney memberGameMoney ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberGameMoney> list = memberGameMoneyService.selectMemberGameMoneyList( memberGameMoney );
        return getRspBasePage( list, pageDomain );
    }
}