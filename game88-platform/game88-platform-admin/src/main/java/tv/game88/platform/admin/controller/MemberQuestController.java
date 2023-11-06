package tv.game88.platform.admin.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.quest.entity.MemberQuest;
import tv.game88.platform.api.service.MemberQuestService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 会员任务列表Controller
 *
 * @author jake from 77tv
 * @date 2021-08-04
 */
@RestController
@RequestMapping("/member/memberQuest")
public class MemberQuestController extends BaseController {

    @Resource
    private MemberQuestService memberQuestService;

    /**
     * 查询会员任务列表
     */
    @PreAuthorize("@ss.hasPermi('member:memberQuest:list')")
    @GetMapping("/list")
    public RspBase<List<MemberQuest>> list(MemberQuest memberQuest) {
        PageDomain pages = TableSupport.buildPageRequest();
        startPage(pages);
        List<MemberQuest> list = memberQuestService.selectMemberQuestList(memberQuest);
        return getRspBasePage(list, pages);
    }

    /**
     * 增加会员积分
     */
    @PreAuthorize("@ss.hasPermi('member:memberQuest:edit')")
    @Log(title = "【会员任务】", businessType = BusinessType.UPDATE)
    @PostMapping("/addScore")
    public RspBase addScore(@RequestBody MemberQuest memberQuest) {
        return toResult(memberQuestService.addMemberScore(memberQuest));
    }

    /**
     * 导出会员任务列表
     */
    @PreAuthorize("@ss.hasPermi('member:memberQuest:export')")
    @Log(title = "【会员任务】", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(MemberQuest memberQuest, HttpServletResponse response) {
        List<MemberQuest> list = memberQuestService.selectMemberQuestList(memberQuest);
        ExportExcelUtil.exportExcel(list, "【请填写功能名称】", "【请填写功能名称】表", MemberQuest.class, response);
    }

    /**
     * 获取会员任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('member:memberQuest:query')")
    @GetMapping(value = "/{id}")
    public RspBase getInfo(@PathVariable("id") String id) {
        return RspBase.ok(memberQuestService.selectMemberQuestById(id));
    }

    /**
     * 新增会员任务
     */
    @PreAuthorize("@ss.hasPermi('member:memberQuest:add')")
    @Log(title = "【会员任务】", businessType = BusinessType.INSERT)
    @PostMapping
    public RspBase add(@RequestBody MemberQuest memberQuest) {
        return toResult(memberQuestService.insertMemberQuest(memberQuest));
    }

    /**
     * 修改会员任务
     */
    @PreAuthorize("@ss.hasPermi('member:memberQuest:edit')")
    @Log(title = "【会员任务】", businessType = BusinessType.UPDATE)
    @PutMapping
    public RspBase edit(@RequestBody MemberQuest memberQuest) {
        return toResult(memberQuestService.updateMemberQuest(memberQuest));
    }

    /**
     * 删除会员任务
     */
    @PreAuthorize("@ss.hasPermi('member:memberQuest:remove')")
    @Log(title = "【会员任务】", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public RspBase remove(@PathVariable String[] ids) {
        return toResult(memberQuestService.deleteMemberQuestByIds(ids));
    }
}
