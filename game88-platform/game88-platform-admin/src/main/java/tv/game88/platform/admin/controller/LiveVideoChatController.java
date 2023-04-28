package tv.game88.platform.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.token.TokenService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.admin.vo.LoginUser;
import tv.game88.core.member.entity.MemberBcode;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.platform.api.entity.LiveVideoChat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.github.pagehelper.page.PageMethod.startPage;

/**
 * 会员发言Controller
 *
 * @author 77tv
 * @date 2021-01-26
 */
@RestController
@RequestMapping("/admin/liveVideoChat")
public class LiveVideoChatController extends BaseController {
    @Autowired
//	private ILiveVideoChatService liveVideoChatService;
//	@Autowired
//	private TokenService tokenService;
//	@Autowired
//	private MemberInfoMapper memberInfoMapper;

    /**
     * 查询会员发言列表
     */
//	@PreAuthorize( "@ss.hasPermi('admin:liveVideoChat:list')" )
    @GetMapping("/list")
    public RspBase list() {

        LiveVideoChat c = new LiveVideoChat();
        c.setId(Long.valueOf(1));
        c.setPoscatId(Long.valueOf(1));
        c.setCreateTime(LocalDateTime.now());
        c.setStatus(1);
        c.setType(1);
        c.setMsg("a");
        c.setUserId(Long.valueOf(2));
        c.setUserIp("1.1.1.1");
        c.setPoscatNickName("jake");
        c.setUserNickName("jakenick");
        c.setFromPlatform("from");
        c.setForbid(true);
        c.setNoSpeaking(true);
//		c.setSendStartTime(LocalDateTime.now().minusHours(10));

//				c.setSendEndTime(LocalDateTime.now().plusHours(10));

        List<LiveVideoChat> a = new ArrayList<>();
        a.add(c);


//		startPage();
//		List<LiveVideoChat> list = liveVideoChatService.selectLiveVideoChatList( liveVideoChat );
//
//		liveVideoChatService.setSpeakForbid( list );
//
//		return getDataTable( a );

//        PageDomain pageDomain = TableSupport.buildPageRequest();
//        startPage(pageDomain);
//        return getRspBasePage(a, pageDomain);
        RspBase aw = getRspBasePage( a );
        aw.setTotal(Long.valueOf(1));
        aw.setHasNext(false);
		return getRspBasePage( a );

//		return RspBase.ok();
    }

    /**
     * 查询会员发言列表
     */
    @PreAuthorize("@ss.hasPermi('admin:liveVideoChat:list')")
    @GetMapping("/listLiveVideoPushChat")
    public RspBase listLiveVideoPushChat(LiveVideoChat liveVideoChat) {
//		startPage();
//		List<LiveVideoChat> list = liveVideoChatService.selectLiveVideoPushChatList( liveVideoChat );
//
//		return getDataTable( list );
        return RspBase.ok();
    }

    /**
     * 直播间用户封停
     *
     * @return
     */
    @PostMapping("suspendUser")
    @Log(title = "用户封停", businessType = BusinessType.UPDATE)
    public RspBase suspendUser(HttpServletRequest request,
                               @RequestBody Map<String, Object> requestMap) {
//		String  pUserId = ( String ) requestMap.get( "pUserId" );
//		boolean flag    = ( boolean ) requestMap.get( "flag" );
//		int     num     = ( int ) requestMap.get( "num" );
//		String  userIp  = ( String ) requestMap.get( "userIp" );
//		String  msg     = ( String ) requestMap.get( "msg" );
//		if ( !StringUtils.hasText( pUserId ) ) {
//			return AjaxResult.error( "会员平台ID不得为空" );
//		}
//		LoginUser loginUser  = tokenService.getLoginUser( ServletUtil.getHttpServletRequest() );
//		String    banAccount = loginUser.getUser().getUserName();
//		return liveVideoChatService.suspendUser( pUserId, flag, num, userIp, msg, banAccount );
        return RspBase.ok();
    }

    /**
     * 直播间用户禁言10分钟
     *
     * @return
     */
    @PostMapping("forbidSendMsg")
    @Log(title = "用户禁言", businessType = BusinessType.UPDATE)
    public RspBase forbidSendMsg(@RequestBody Map<String, Object> requestMap) {
//		String  pUserId    = ( String ) requestMap.get( "pUserId" );
//		Integer videoId    = ( Integer ) requestMap.get( "videoId" );
//		String  remark     = ( String ) requestMap.get( "remark" );
//		Integer forbidTime = 600;
//		if ( !StringUtils.hasText( pUserId ) ) {
////			return AjaxResult.error( "会员平台ID不得为空" );
//			RspBase.ok("会员平台ID不得为空");
//		}
//		//禁言添加备注
//		LoginUser loginUser = SecurityUtils.getLoginUser();
////		LoginUser  loginUser  = tokenService.getLoginUser( ServletUtil.getHttpServletRequest() );
//		String     username   = loginUser.getUser().getUserName();
//		MemberInfo memberInfo = new MemberInfo();
//		memberInfo.setId( pUserId );
//		memberInfo.setRemark( "禁言操作人:" + username + ";禁言10分钟原因:" + remark );
//		memberInfoMapper.updateById( memberInfo );
//		liveVideoChatService.forbidSendMsg( pUserId, forbidTime, videoId );
        return RspBase.ok();
    }

    /**
     * 导出会员发言列表
     */
    @PreAuthorize("@ss.hasPermi('admin:liveVideoChat:export')")
    @Log(title = "会员发言", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(LiveVideoChat liveVideoChat, HttpServletResponse response) {
//		List<LiveVideoChat> list = liveVideoChatService.selectLiveVideoChatList( liveVideoChat );
//		ExportExcelUtil.exportExcel( list, "会员发言", "会员发言表", LiveVideoChat.class, response );
    }

    /**
     * 获取会员发言详细信息
     */
    @PreAuthorize("@ss.hasPermi('admin:liveVideoChat:query')")
    @GetMapping(value = "/{id}")
    public RspBase getInfo(@PathVariable("id") Long id) {
//		return AjaxResult.success( liveVideoChatService.selectLiveVideoChatById( id ) );
        return RspBase.ok();
    }


    /**
     * 新增会员发言
     */
    @PreAuthorize("@ss.hasPermi('admin:liveVideoChat:add')")
    @Log(title = "会员发言", businessType = BusinessType.INSERT)
    @PostMapping
    public RspBase add(@RequestBody LiveVideoChat liveVideoChat) {
//		return toAjax( liveVideoChatService.insertLiveVideoChat( liveVideoChat ) );
        return RspBase.ok();
    }


    /**
     * 修改会员发言
     */
    @PreAuthorize("@ss.hasPermi('admin:liveVideoChat:edit')")
    @Log(title = "会员发言", businessType = BusinessType.UPDATE)
    @PutMapping
    public RspBase edit(@RequestBody LiveVideoChat liveVideoChat) {
//		return toAjax( liveVideoChatService.updateLiveVideoChat( liveVideoChat ) );
        return RspBase.ok();
    }

    /**
     * 删除会员发言
     */
    @PreAuthorize("@ss.hasPermi('admin:liveVideoChat:remove')")
    @Log(title = "会员发言", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public RspBase remove(@PathVariable Long[] ids) {
//		return toAjax( liveVideoChatService.deleteLiveVideoChatByIds( ids ) );
        return RspBase.ok();
    }
}
