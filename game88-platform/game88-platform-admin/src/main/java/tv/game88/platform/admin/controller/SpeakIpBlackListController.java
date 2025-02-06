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
import tv.game88.platform.api.entity.SpeakIpBlackList;
import tv.game88.platform.api.service.SpeakIpBlackListService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/speakIpBlackList")
public class SpeakIpBlackListController extends BaseController {
    @Resource
    private SpeakIpBlackListService speakIpBlackListService;

    @GetMapping("/list")
    public RspBase<List<SpeakIpBlackList>> list(SpeakIpBlackList speakIpBlackList) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage(pageDomain);
//        List<SpeakIpBlackList> list = speakIpBlackListService.selectSpeakIpBlackListList(speakIpBlackList);

        SpeakIpBlackList a = new SpeakIpBlackList();
        a.setId(1);
        a.setMsg("Message ban");
        a.setUserId("01");
        a.setCreateTime(LocalDateTime.now());
        a.setUserIp("192.192.192.192");

        List<SpeakIpBlackList> list = new ArrayList<>();
        list.add(a);

        return getRspBasePage(list, pageDomain);
    }

    @PreAuthorize("@ss.hasPermi('admin:speakIpBlackList:export')")
    @Log(title = "封禁IP", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(SpeakIpBlackList speakIpBlackList, HttpServletResponse response) {
        List<SpeakIpBlackList> list = speakIpBlackListService.selectSpeakIpBlackListList(speakIpBlackList);
        ExportExcelUtil.exportBigExcel(list, "禁言IP", "禁言IP表", SpeakIpBlackList.class, response);
    }

    @PreAuthorize("@ss.hasPermi('admin:speakIpBlackList:query')")
    @GetMapping(value = "/{id}")
    public RspBase<SpeakIpBlackList> getInfo(@PathVariable("id") String id) {
        return RspBase.ok(speakIpBlackListService.selectSpeakIpBlackListById(id));
    }

    @PreAuthorize("@ss.hasPermi('admin:speakIpBlackList:add')")
    @Log(title = "封禁IP", businessType = BusinessType.INSERT)
    @PostMapping
    public RspBase<?> add(@RequestBody SpeakIpBlackList speakIpBlackList) {
        return toResult(speakIpBlackListService.insertSpeakIpBlackList(speakIpBlackList));
    }

    @PreAuthorize("@ss.hasPermi('admin:speakIpBlackList:remove')")
    @Log(title = "封禁IP", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public RspBase<?> remove(@PathVariable String[] ids) {
        return toResult(speakIpBlackListService.deleteSpeakIpBlackListByIds(ids));
    }
}