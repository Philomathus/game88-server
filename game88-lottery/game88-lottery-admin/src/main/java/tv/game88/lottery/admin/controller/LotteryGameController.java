package tv.game88.lottery.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.lottery.api.entity.LotteryGame;
import tv.game88.lottery.api.service.LotteryGameService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 下注Controller
 *
 * @author mengJun
 * @date 2022-12-21
 */
@RestController
@RequestMapping("/lottery/game")
public class LotteryGameController extends BaseController {

    @Resource
    private LotteryGameService lotteryGameService;

    /**
     * 查询下注列表 list all data
     */
    @PreAuthorize( "@ss.hasPermi('lottery:game:list')" )
    @GetMapping("/list")
    public RspBase<List<LotteryGame>> list(LotteryGame lotteryGame){
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<LotteryGame> list = lotteryGameService.selectLotteryGameAll( lotteryGame );
        return getRspBasePage( list,pageDomain );
    }

    /**
     * 获取下注详细信息 get data by id
     */
    @PreAuthorize( "@ss.hasPermi('lottery:game:query')" )
    @GetMapping("/{id}")
    public RspBase<LotteryGame> getById( @PathVariable  Integer id){
        return RspBase.ok(lotteryGameService.getById( id ));
    }

    /**
     * 修改下注 update data
     */
    @PreAuthorize( "@ss.hasPermi('lottery:game:edit')" )
    @Log( title = "修改下注", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody LotteryGame lotteryGame ) {
        return toResult( lotteryGameService.updateById( lotteryGame ) );
    }

}
