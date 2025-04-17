package tv.game88.game.admin.controllor;

import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.mapper.MemberInfoMapper;
import tv.game88.game.api.entity.MemberGameDataFix;
import tv.game88.game.api.service.MemberGameDataFixService;

import java.util.Arrays;
import java.util.List;

/**
 * 游戏补单Controller
 *
 * @author 77tv
 * {@code @date} 2021-01-29
 */
@RestController
@RequestMapping( "/game/memberGameDatafix" )
public class MemberGameDatafixController extends BaseController {
    @Resource
    private MemberGameDataFixService memberGameDataFixService;
    @Resource
    private MemberInfoMapper         memberInfoMapper;

    /**
     * 查询游戏补单列表
     */
    @PreAuthorize( "@ss.hasPermi('game:memberGameDatafix:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberGameDataFix>> list( MemberGameDataFix memberGameDatafix ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberGameDataFix> list = memberGameDataFixService.selectMemberGameDataFixList( memberGameDatafix );
        return getRspBasePage( list, pageDomain );
    }


    /**
     * 新增游戏补单
     */
    @PreAuthorize( "@ss.hasPermi('game:memberGameDatafix:add')" )
    @Log( title = "新增补单", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody MemberGameDataFix memberGameDatafix ) {
        memberGameDatafix.setStatus( 0 );
        if ( !StringUtils.isEmpty( memberGameDatafix.getMemberId() ) ) {
            MemberInfo memberInfo = memberInfoMapper.selectById( memberGameDatafix.getMemberId() );
            if ( memberInfo == null ) {
                return RspBase.businessError( "用户不存在" );
            }
        }
        return toResult( memberGameDataFixService.save( memberGameDatafix ) );
    }

    /**
     * 导出游戏注单修复列表
     */
    @PreAuthorize( "@ss.hasPermi('game:memberGameDatafix:export')" )
    @Log( title = "游戏注单修复", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public RspBase<List<MemberGameDataFix>> export( MemberGameDataFix memberGameDatafix ) {
        return RspBase.ok(  memberGameDataFixService.selectMemberGameDataFixList( memberGameDatafix ) );
    }

    /**
     * 删除游戏注单修复
     */
    @PreAuthorize( "@ss.hasPermi('game:memberGameDatafix:remove')" )
    @Log( title = "游戏注单修复", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{ids}" )
    public RspBase<?> remove( @PathVariable String[] ids ) {
        return toResult( memberGameDataFixService.removeBatchByIds( Arrays.asList( ids ) ) );
    }
}
