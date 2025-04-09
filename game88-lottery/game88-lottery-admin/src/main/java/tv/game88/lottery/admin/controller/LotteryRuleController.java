package tv.game88.lottery.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.lottery.api.entity.LotteryRule;
import tv.game88.lottery.api.service.LotteryRuleService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 彩票规则说明Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/lottery/lotteryRule" )
public class LotteryRuleController extends BaseController {
	@Resource
	private LotteryRuleService lotteryRuleService;

	/**
	 * 查询彩票规则说明列表
	 */
	@PreAuthorize( "@ss.hasPermi('lottery:rule:list')" )
	@GetMapping( "/list" )
	public RspBase<List<LotteryRule>> list(LotteryRule lotteryRule) {
		PageDomain pageDomain = TableSupport.buildPageRequest();
		startPage( pageDomain );
		List<LotteryRule> list = lotteryRuleService.selectLotteryRuleList(lotteryRule);
		return getRspBasePage( list, pageDomain );
	}

	/**
	 * 导出彩票规则说明列表
	 */
	@PreAuthorize( "@ss.hasPermi('lottery:rule:export')" )
	@Log( title = "彩票规则说明", businessType = BusinessType.EXPORT )
	@GetMapping( "/export" )
	public RspBase<List<LotteryRule>> export(LotteryRule lotteryRule, HttpServletResponse response) {
		List<LotteryRule>      list = lotteryRuleService.selectLotteryRuleList(lotteryRule);
//		ExportExcelUtil.exportBigExcel( list, "彩票规则说明", "彩票规则说明表", LotteryRule.class, response );
		return RspBase.ok( list );
	}

	/**
	 * 获取彩票规则说明详细信息
	 */
	@PreAuthorize( "@ss.hasPermi('lottery:rule:query')" )
	@GetMapping( value = "/{id}" )
	public RspBase<LotteryRule> getInfo( @PathVariable( "id" ) Integer id) {
		return RspBase.ok( lotteryRuleService.getById(id) );
	}

	/**
	 * 新增彩票规则说明
	 */
	@PreAuthorize( "@ss.hasPermi('lottery:rule:add')" )
	@Log( title = "彩票规则说明", businessType = BusinessType.INSERT )
	@PostMapping
	public RspBase<?> add( @RequestBody LotteryRule lotteryRule) {
		return toResult( lotteryRuleService.save(lotteryRule) );
	}

	/**
	 * 修改彩票规则说明
	 */
	@PreAuthorize( "@ss.hasPermi('lottery:rule:edit')" )
	@Log( title = "彩票规则说明", businessType = BusinessType.UPDATE )
	@PutMapping
	public RspBase<?> edit( @RequestBody LotteryRule lotteryRule) {
		return toResult( lotteryRuleService.updateById(lotteryRule) );
	}

	/**
	 * 删除彩票规则说明
	 */
	@PreAuthorize( "@ss.hasPermi('lottery:rule:remove')" )
	@Log( title = "彩票规则说明", businessType = BusinessType.DELETE )
	@DeleteMapping( "/{ids}" )
	public RspBase<?> remove( @PathVariable Integer[] ids ) {
		return toResult( lotteryRuleService.removeByIds( Arrays.asList( ids ) ) );
	}
}