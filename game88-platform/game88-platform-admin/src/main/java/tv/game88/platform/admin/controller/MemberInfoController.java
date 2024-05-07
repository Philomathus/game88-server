package tv.game88.platform.admin.controller;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tv.game88.common.base.BaseController;
import tv.game88.common.exception.BusinessException;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.common.utils.ServletUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.config.constants.Constants;
import tv.game88.core.member.dto.ReqSmallFeatures;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.entity.MemberInfo;
import tv.game88.core.member.vo.PlatformUser;
import tv.game88.platform.api.dto.ReqAddScore;
import tv.game88.platform.api.service.MemberInfoService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户信息Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/member/memberInfo" )
@Log4j2
public class MemberInfoController extends BaseController {
    @Resource
    private MemberInfoService memberInfoService;
    @Resource
    private PasswordEncoder   passwordEncoder;
    @Resource
    private RedisUtils        redisUtil;

    /**
     * 查询用户信息列表
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:list')" )
    @GetMapping( "/list" )
    public RspBase<List<MemberInfo>> list( MemberInfo memberInfo ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberInfo> list = memberInfoService.selectMemberInfoList( memberInfo );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 导出用户信息列表
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:export')" )
    @Log( title = "导出", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( MemberInfo memberInfo, HttpServletResponse response ) {
        List<MemberInfo> list = memberInfoService.selectMemberInfoList( memberInfo );
        if ( list.size() <= 200000L ) {
            ExportExcelUtil.exportExcel( list, "用户信息", "用户信息表", MemberInfo.class, response );
        } else {
            throw new BusinessException( "导出条数超过20万条" );
        }
    }

    /**
     * 获取用户信息详细信息
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<MemberInfo> getInfo( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberInfoService.getById( id ) );
    }

    /**
     * 统计会员余额
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:query')" )
    @GetMapping( "/listCount" )
    public Map listCount( MemberInfo memberInfo ) {
        return memberInfoService.listCount( memberInfo );
    }

    /**
     * 获取完整手机号
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:fullMobile')" )
    @GetMapping( value = "/fullMobile/{id}" )
    public RspBase<MemberInfo> fullMobile( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberInfoService.getById( id ) );
    }

    /**
     * 获取用户线上充值历史金额
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:query')" )
    @GetMapping( value = "/getHistoryRecharge/{memberId}" )
    public RspBase<BigDecimal> getHistoryRecharge( @PathVariable( "memberId" ) String id ) {
        return RspBase.ok( memberInfoService.getHistoryRecharge( id ) );
    }

    /**
     * 获取用户登录地址
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:query')" )
    @GetMapping( value = "/getMemberLoginAddress/{id}" )
    public RspBase<String> getMemberLoginAddress( @PathVariable( "id" ) String id ) {
        return RspBase.ok( memberInfoService.getMemberLoginAddress( id ) );
    }

    /**
     * 新增用户信息
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:add')" )
    @Log( title = "用户信息", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody MemberInfo memberInfo ) {
        return memberInfoService.insertMemberInfo( memberInfo.getPhone(), memberInfo.getPassword() );
    }

    @PreAuthorize( "@ss.hasPermi('member:memberInfo:changeStatus')" )
    @Log( title = "修改用户状态", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeStatus/{memberId}" )
    public Object changeStatusBan( @PathVariable String memberId, Integer status, Integer googleAuthCode,
                                   @RequestParam( required = false ) String remark ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setStatus( status );
        if ( StringUtils.isNotBlank( remark ) && status == 0 ) {
            update.setRemark( "禁用操作人" + SecurityUtils.getUsername() + ";禁用原因:" + remark );
        }
        if ( status == 1 ) {
            update.setLoginNum( 0 );
        }

        boolean b = memberInfoService.updateById( update );
        if ( b ) {
            String token = redisUtil.strGet( Constants.MEMBER_LOGIN_USER + memberId );
            if ( StringUtils.isNotBlank( token ) && status == 0 ) {
                redisUtil.unlink( Constants.MEMBER_LOGIN_TOKEN + token, Constants.MEMBER_LOGIN_USER + memberId );
            } else if ( StringUtils.isNotBlank( token ) && redisUtil.exists( Constants.MEMBER_LOGIN_TOKEN + token ) ) {
                String platformUserStr = redisUtil.hGet( Constants.MEMBER_LOGIN_TOKEN + token, "platformUserStr" ).toString();
                PlatformUser platformUser = JsonUtil.json2Object( platformUserStr, PlatformUser.class );
                platformUser.setStatus( status );
                redisUtil.hSet( Constants.MEMBER_LOGIN_TOKEN + token, "platformUserStr", JsonUtil.object2Json( platformUser ) );
            }
        }
        return toResult( b );
    }

    /**
     * 重置登录密码
     */
    @PostMapping( value = "/resetPasswd/{memberId}" )
    @Log( title = "重置会员登录密码", businessType = BusinessType.UPDATE )
    public RspBase<?> reset( @PathVariable String memberId, String passwd, Integer googleAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );

        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setPassword( passwordEncoder.encode( passwd ) );// 这里使用管理员的加密方法和会员的是一样的
        boolean b = memberInfoService.updateById( update );
        if ( b ) {
            String token = redisUtil.strGet( Constants.MEMBER_LOGIN_USER + memberId );
            if ( StringUtils.isNotBlank( token ) ) {
                redisUtil.unlink( Constants.MEMBER_LOGIN_TOKEN + token, Constants.MEMBER_LOGIN_USER + memberId );
            }
        }
        return toResult( b );
    }

    /**
     * 加分
     */
    @PostMapping( value = "/addScore" )
    @Log( title = "加分", businessType = BusinessType.UPDATE )
    public RspBase<?> addScore( @RequestBody ReqAddScore req ) throws Exception {
        SecurityUtils.verifyMFACode( req.getGoogleAuthCode() );
        if ( !redisUtil.lock( "memberAddScore" + req.getId(), 15 ) ) {
            return RspBase.businessError( "请勿重复提交" );
        }
        return memberInfoService.addMemberMoneyOnly( ServletUtil.getIp(), SecurityUtils.getUsername(), req );
    }

    /**
     * 发送短信
     */
    @RequestMapping( value = "/sendMsg", method = RequestMethod.POST )
    @Log( title = "会员发送短信", businessType = BusinessType.UPDATE )
    public RspBase sendMsg( @RequestBody Map map ) {
        RspBase rspBase  = new RspBase();
        String  msg      = ( String ) map.get( "msg" );
        String  memberId = ( String ) map.get( "memberId" );
        if ( StringUtils.isNotBlank( msg ) && StringUtils.isNotBlank( memberId ) ) {
            memberInfoService.sendMsg( msg, memberId );
            rspBase.setMsg( "发送成功" );
        } else {
            rspBase.setMsg( "发送失败" );
        }
        return rspBase;
    }

    /**
     * 修改邀请码
     */
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:updateInviterCode')" )
    @PostMapping( value = "/updateInviterCode/{memberId}" )
    @Log( title = "会员修改邀请码", businessType = BusinessType.UPDATE )
    public RspBase<?> updateInviterCode( @PathVariable String memberId, Integer googleAuthCode, String inviterCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setInviterCode( inviterCode );
        return toResult( memberInfoService.updateById( update ) );
    }

    /**
     * 更新会员手机号
     */
    @PostMapping( value = "/updateMobile/{memberId}" )
    @Log( title = "更新会员手机号", businessType = BusinessType.UPDATE )
    public RspBase<?> updateMobile( @PathVariable String memberId, @RequestBody Map map ) throws Exception {
        String newMobile      = ( String ) map.get( "newMobile" );
        String phone          = ( String ) map.get( "oldMobile" );
        int    googleAuthCode = Integer.parseInt( ( java.lang.String ) map.get( "googleAuthCode" ) );

        SecurityUtils.verifyMFACode( googleAuthCode );
        return memberInfoService.updateMobile( phone, newMobile, memberId );
    }

    /**
     * 会员银行卡列表
     */
    @GetMapping( value = "/card-list" )
    public RspBase<List<MemberCard>> findMemberCardList( String memberId ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<MemberCard> list = memberInfoService.selectMemberCardList( memberId );
        for ( MemberCard memberCard : list ) {
            memberCard.setOldBankAccount( memberCard.getBankAccount() );
            memberCard.setOldBankId( memberCard.getBankId() );
            memberCard.setOldRealName( memberCard.getRealName() );
        }
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 重置保险箱密码
     *
     * @param memberId
     */
    @PostMapping( "/resetBoxPasswd/{memberId}" )
    public Object resetPassword( @PathVariable String memberId, Integer googleAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setBoxPass( "" );
        return toResult( memberInfoService.updateById( update ) );
    }

    /**
     * 重置提现密码
     *
     * @param memberId
     * @param googleAuthCode
     */
    @PostMapping( "/resetTxPass/{memberId}" )
    public Object resetTxPass( @PathVariable String memberId, Integer googleAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );

        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setWithdrawalPass( "" );
        return toResult( memberInfoService.updateById( update ) );
    }

    /**
     * 修复打码
     *
     * @param memberId
     * @param googleAuthCode
     */
    @Log( title = "修复打码", businessType = BusinessType.UPDATE )
    @PostMapping( "/memberBcodeRepair/{memberId}" )
    public Object memberBcodeRepair( @PathVariable String memberId, Integer googleAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        return toResult( memberInfoService.repairMemberBcode( memberId ) );
    }

    @Log( title = "修改vip等级", businessType = BusinessType.UPDATE )
    @PostMapping( "/updateVip/{memberId}" )
    public RspBase<?> updateVip( @PathVariable String memberId, Integer vip, String nickName ) {
        return memberInfoService.updateVip( memberId, vip, nickName );
    }

    @PreAuthorize( "@ss.hasPermi('member:memberInfo:unbindCard')" )
    @Log( title = "解绑银行卡", businessType = BusinessType.UPDATE )
    @PutMapping( "/unbindCard" )
    public RspBase<?> unbindCard( @RequestBody MemberCard memberCard ) {
        return memberInfoService.unbindCard( memberCard );
    }

    @PreAuthorize( "@ss.hasPermi('member:memberInfo:changeBank')" )
    @Log( title = "修改用户银行卡信息", businessType = BusinessType.UPDATE )
    @PutMapping( "/changeBank" )
    public RspBase<?> changeBank( @RequestBody MemberCard memberCard ) {
        return memberInfoService.changeBank( memberCard );
    }

    @Log( title = "修改用户备注", businessType = BusinessType.UPDATE )
    @PutMapping( "/updateRemark/{memberId}" )
    public RspBase<?> updateEmail( @PathVariable String memberId, String remark ) {
        MemberInfo update = new MemberInfo();
        update.setId( memberId );
        update.setRemark( remark );
        return toResult( memberInfoService.updateById( update ) );
    }

    /**
     * 会员个人报表
     *
     * @param memberId
     * @param request
     */
    @GetMapping( "/personal-report/{memberId}" )
    public RspBase<?> personalReport( @PathVariable String memberId, String[] date, HttpServletRequest request ) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String                startTime    = parameterMap.get( "date[0]" )[ 0 ];
        String                endTime      = parameterMap.get( "date[1]" )[ 0 ];
        return memberInfoService.personalReport( startTime, endTime, memberId );
    }

    @Log( title = "保险箱余额转出", businessType = BusinessType.UPDATE )
    @PostMapping( "/boxDish/{memberId}" )
    public RspBase<?> boxDish( @PathVariable String memberId, Integer googleAuthCode ) throws Exception {
        SecurityUtils.verifyMFACode( googleAuthCode );
        return memberInfoService.boxDish( memberId );
    }

    @PostMapping( "/batchUploadExcel" )
    @Transactional( rollbackFor = Exception.class )
    public RspBase<?> batchUploadExcel( @RequestParam( "excelFile" ) MultipartFile excelFile ) throws Exception {
        Workbook      workbook = null;
        StringBuilder userId   = new StringBuilder();
        try {
            workbook = WorkbookFactory.create( excelFile.getInputStream() );
            excelFile.getInputStream().close();
            //工作表对象
            Sheet sheet = workbook.getSheetAt( 0 );
            //总行数
            int rowLength = sheet.getLastRowNum() + 1;
            //工作表的列
            Row row = sheet.getRow( 0 );
            //总列数
            //            int colLength = row.getLastCellNum();
            //得到指定的单元格
            for ( int i = 0; i < rowLength; i++ ) {
                Cell cell = row.getCell( i );
                row = sheet.getRow( i );
                String cell1 = null;
                String cell2 = null;
                String cell3 = null;
                for ( int j = 0; j < 3; j++ ) {
                    cell = row.getCell( j );
                    if ( cell != null ) {
                        cell.setCellType( CellType.STRING );
                        String data = cell.getStringCellValue();
                        if ( j == 0 ) {
                            cell1 = data.trim();
                        } else if ( j == 1 ) {
                            cell2 = data.trim();
                        } else {
                            cell3 = data.trim();
                        }
                    }
                }
                if ( StringUtils.isBlank( cell1 ) || StringUtils.isBlank( cell2 ) ) {
                    break;
                }
                if ( StringUtils.isBlank( cell3 ) ) {
                    cell3 = "1";
                }
                userId = userId
                        .append( "\"" )
                        .append( cell1 )
                        .append( "\"" )
                        .append( "," )
                        .append( cell2 )
                        .append( "," )
                        .append( cell3 )
                        .append( "),(" );
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        userId = new StringBuilder( userId.substring( 0, userId.length() - 3 ) );
        String userIds = String.valueOf( userId );


        return memberInfoService.insertBatchExcelMoney( userIds );
    }

    @PostMapping( value = "/memberSmallFeatures" )
    public Object memberSmallFeatures( ReqSmallFeatures req ) throws Exception {
        SecurityUtils.verifyMFACode( req.getGoogleAuthCode() );
        return RspBase.ok( memberInfoService.updatePhones( req ) );
    }

    @PostMapping( value = "/queryPhones" )
    public Object queryPhones( @RequestBody ReqSmallFeatures req ) throws Exception {
        SecurityUtils.verifyMFACode( req.getGoogleAuthCode() );
        return RspBase.ok( memberInfoService.queryPhones( req ) );
    }

    /**
     * 批量会员ID派送彩金
     */
    @PostMapping( value = "/commitMoney" )
    public Object commitMoney( @RequestBody ReqSmallFeatures req ) throws Exception {
        SecurityUtils.verifyMFACode( req.getGoogleAuthCode() );
        return RspBase.ok( memberInfoService.commitMoney( req ) );
    }

    @RequestMapping( value = "/batchInsertShops", method = RequestMethod.POST )
    @Transactional( rollbackFor = Exception.class )
    public Object batchInsert( @RequestParam( "excelFile" ) MultipartFile excelFile ) throws Exception {
        Workbook      workbook = null;
        StringBuilder userId   = new StringBuilder();
        try {
            workbook = WorkbookFactory.create( excelFile.getInputStream() );
            excelFile.getInputStream().close();
            //工作表对象
            Sheet sheet = workbook.getSheetAt( 0 );
            //总行数
            int rowLength = sheet.getLastRowNum() + 1;
            //工作表的列
            Row row = sheet.getRow( 0 );
            //总列数
            //得到指定的单元格
            for ( int i = 0; i < rowLength; i++ ) {
                Cell cell = row.getCell( i );
                row = sheet.getRow( i );
                String cell1 = null;
                String cell2 = null;
                String cell3 = null;
                for ( int j = 0; j < 3; j++ ) {
                    cell = row.getCell( j );
                    if ( cell != null ) {
                        cell.setCellType( CellType.STRING );
                        String data = cell.getStringCellValue();
                        if ( j == 0 ) {
                            cell1 = data.trim();
                        } else if ( j == 1 ) {
                            cell2 = data.trim();
                        } else {
                            cell3 = data.trim();
                        }
                    }
                }
                if ( StringUtils.isBlank( cell1 ) || StringUtils.isBlank( cell2 ) ) {
                    break;
                }
                if ( StringUtils.isBlank( cell3 ) ) {
                    cell3 = "1";
                }
                userId = userId
                        .append( "\"" )
                        .append( cell1 )
                        .append( "\"" )
                        .append( "," )
                        .append( cell2 )
                        .append( "," )
                        .append( cell3 )
                        .append( "),(" );
            }
        } catch ( Exception e ) {
            log.error( e.getMessage(), e );
        }
        userId = new StringBuilder( userId.substring( 0, userId.length() - 3 ) );
        String userIds = String.valueOf( userId );
        //清除表中数据
        memberInfoService.clear();
        memberInfoService.insertPaiSong( userIds );
        return RspBase.ok();
    }


    /**
     * 出款状态
     */
    @Log( title = "激活状态", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeWithdrawStatus/{id}/{withdrawStatus}" )
    public RspBase<?> changeWithdrawStatus( @PathVariable String id, @PathVariable Boolean withdrawStatus ) {
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setId( String.valueOf( id ) );
        memberInfo.setWithdrawStatus( withdrawStatus );
        boolean isUpdate = memberInfoService.updateById( memberInfo );
        return toResult( isUpdate );
    }

    @Log( title = "修改总打码和VIP等级", businessType = BusinessType.UPDATE )
    @PreAuthorize( "@ss.hasPermi('member:memberInfo:editCode')" )
    @PutMapping( "/updateCodeTotal" )
    public RspBase<?> updateCodeTotal( @RequestBody MemberInfo memberInfo ) {
        return RspBase.ok( memberInfoService.updateCodeTotalVipLevel( memberInfo ) );
    }


}
