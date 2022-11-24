package tv.game88.pay.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tv.game88.common.base.BaseController;
import tv.game88.common.page.PageDomain;
import tv.game88.common.page.TableSupport;
import tv.game88.common.utils.ExportExcelUtil;
import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.annotation.Log;
import tv.game88.core.admin.enums.BusinessType;
import tv.game88.core.admin.utils.SecurityUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.pay.api.cache.PayCacheUtil;
import tv.game88.pay.api.entity.PayChannel;
import tv.game88.pay.api.entity.PayType;
import tv.game88.pay.api.service.PayChannelService;
import tv.game88.pay.api.service.PayTypeService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付类型Controller
 *
 * @author mengJun
 */
@RestController
@RequestMapping( "/pay/payType" )
public class PayTypeController extends BaseController {
    @Resource
    private PayTypeService    payTypeService;
    @Resource
    private PayChannelService payChannelService;
    @Resource
    private PayCacheUtil      payCacheUtil;

    /**
     * 查询支付类型列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payType:list')" )
    @GetMapping( "/list" )
    public RspBase<List<PayType>> list( PayType payType ) {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        startPage( pageDomain );
        List<PayType> list = payTypeService.selectPayTypeList( payType );
        return getRspBasePage( list, pageDomain );
    }

    /**
     * 查询支付类型列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payType:list')" )
    @GetMapping( "/listAll" )
    public RspBase<List<PayType>> listAll() {
        return RspBase.ok( payTypeService.selectPayTypeList( null ) );
    }

    /**
     * 导出支付类型列表
     */
    @PreAuthorize( "@ss.hasPermi('pay:payType:export')" )
    @Log( title = "支付类型", businessType = BusinessType.EXPORT )
    @GetMapping( "/export" )
    public void export( PayType payType, HttpServletResponse response ) {
        List<PayType> list = payTypeService.selectPayTypeList( payType );
        ExportExcelUtil.exportExcel( list, "支付类型", "支付类型表", PayType.class, response );
    }

    /**
     * 获取支付类型详细信息
     */
    @PreAuthorize( "@ss.hasPermi('pay:payType:query')" )
    @GetMapping( value = "/{id}" )
    public RspBase<PayType> getInfo( @PathVariable( "id" ) Integer id ) {
        PayType payType = payTypeService.getById( id );
        if ( payType != null ) {
            payType.setIconUrl( ConfigDomainCacheUtil.me.getDomainOssValue() + payType.getIconUrl() );
        }
        return RspBase.ok( payType );
    }

    /**
     * 新增支付类型
     */
    @PreAuthorize( "@ss.hasPermi('pay:payType:add')" )
    @Log( title = "支付类型", businessType = BusinessType.INSERT )
    @PostMapping
    public RspBase<?> add( @RequestBody PayType payType ) {
        if ( payType.getType() != 1 ) {
            long a = payTypeService.count( new QueryWrapper<PayType>().eq( "type", payType.getType() ) );
            if ( a > 0 ) {
                return RspBase.businessError( "此支付类型已存在,不允许添加多个" );
            }
        }
        payType.setCreateBy( SecurityUtils.getUsername() );
        payType.setCreateTime( LocalDateTime.now() );
        return toResult( payTypeService.save( payType ) );
    }

    /**
     * 修改支付类型
     */
    @PreAuthorize( "@ss.hasPermi('pay:payType:edit')" )
    @Log( title = "支付类型", businessType = BusinessType.UPDATE )
    @PutMapping
    public RspBase<?> edit( @RequestBody PayType payType ) {
        payType.setType( null );
        payType.setUpdateBy( SecurityUtils.getUsername() );
        payType.setUpdateTime( LocalDateTime.now() );
        boolean isUpdate = payTypeService.updateById( payType );
        if ( isUpdate ) {
            payCacheUtil.clearPayType( payType.getId() );
        }
        return toResult( isUpdate );
    }

    /**
     * 删除支付类型
     */
    @PreAuthorize( "@ss.hasPermi('pay:payType:remove')" )
    @Log( title = "支付类型", businessType = BusinessType.DELETE )
    @DeleteMapping( "/{id}" )
    public RspBase<?> remove( @PathVariable Long id ) {
        //查询此支付类型下还有无支付通道
        long a = payChannelService.count( new QueryWrapper<PayChannel>().eq( "type_id", id ).eq( "del_flag", 0 ) );
        if ( a > 0 ) {
            return RspBase.businessError( "此支付类型下还存在支付通道,删除失败" );
        }
        boolean isDel = payTypeService.removeById( id );
        if ( isDel ) {
            payCacheUtil.clearPayType( id );
        }
        return toResult( isDel );
    }

    /**
     * 支付状态修改
     */
    @PreAuthorize( "@ss.hasPermi('pay:payType:effect')" )
    @Log( title = "支付类型激活状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeStatus/{id}/{effect}" )
    public RspBase<?> changeStatus( @PathVariable Long id, @PathVariable Boolean effect ) {
        PayType payType = new PayType();
        payType.setId( id );
        payType.setEffect( effect );
        boolean isSave = payTypeService.updateById( payType );
        if ( isSave ) {
            payCacheUtil.clearPayType( id );
        }
        return toResult( isSave );
    }

    /**
     * 支付状态修改
     */
    @PreAuthorize( "@ss.hasPermi('pay:payType:effect')" )
    @Log( title = "支付类型推荐状态修改", businessType = BusinessType.EFFECT )
    @PutMapping( "/changeRecommend/{id}/{recommend}" )
    public RspBase<?> changeRecommend( @PathVariable Long id, @PathVariable Boolean recommend ) {
        PayType payType = new PayType();
        payType.setId( id );
        payType.setRecommend( recommend );
        boolean isSave = payTypeService.updateById( payType );
        if ( isSave ) {
            payCacheUtil.clearPayType( id );
        }
        return toResult( isSave );
    }
}
