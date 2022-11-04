package tv.game88.common.base;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import tv.game88.common.exception.ControllerExceptionHandler;
import tv.game88.common.page.PageDomain;
import tv.game88.common.utils.LocalDateTimeUtils;
import tv.game88.common.utils.SqlUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;

import java.beans.PropertyEditorSupport;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

/**
 * web层通用数据处理
 *
 * @author MengJun
 */
@Log4j2
public class BaseController extends ControllerExceptionHandler {

    /**
     * 将前台传递过来的日期格式的字符串，自动转化为LocalDateTime类型
     */
    @InitBinder
    public void initBinder( WebDataBinder binder ) {
        // LocalDate 类型转换
        binder.registerCustomEditor( Temporal.class, new PropertyEditorSupport() {
            @Override
            public void setAsText( String text ) {
                if ( text.indexOf( " " ) > 0 ) {
                    setValue( LocalDateTimeUtils.parseLocalDateTime( text ) );
                } else {
                    setValue( LocalDateTimeUtils.parseLocalDate( text ) );
                }
            }
        } );
        // 表单和URL去空格
        binder.registerCustomEditor( String.class, new StringTrimmerEditor( false ) );
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage( PageDomain pageDomain ) {
        Integer pageNum  = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        if ( StringUtils.isNotNull( pageNum ) && StringUtils.isNotNull( pageSize ) ) {
            String orderBy = SqlUtil.escapeOrderBySql( pageDomain.getOrderBy() );
            PageHelper.startPage( pageNum, pageSize, orderBy );
        }
    }

    /**
     * 响应请求分页数据
     */
    protected <T> RspBase<List<T>> getRspBasePage( List<T> list ) {
        RspBase<List<T>> rspData = RspBase.ok();
        if ( list == null ) {
            return rspData;
        }
        rspData.setData( list );
        rspData.setTotal( new PageInfo<>( list ).getTotal() );
        return rspData;
    }

    /**
     * 响应请求分页数据
     */
    protected <T> RspBase<List<T>> getRspBasePage( List<T> list, PageDomain pageDomain ) {
        RspBase<List<T>> rspData = RspBase.ok();
        if ( list == null ) {
            return rspData;
        }
        PageInfo<T> pageInfo = new PageInfo<>( list );
        rspData.setTotal( pageInfo.getTotal() );
        if ( pageDomain.getPageNum() > pageInfo.getPages() ) {
            rspData.setData( new ArrayList<>() );
            return rspData;
        }
        rspData.setData( list );
        Integer countSum = pageDomain.getPageSize() * pageDomain.getPageNum();
        if ( rspData.getTotal() > countSum ) {
            rspData.setHasNext( true );
        }
        return rspData;
    }

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     *
     * @return 操作结果
     */
    protected RspBase<?> toResult( int rows ) {
        return rows > 0 ? RspBase.ok() : RspBase.businessError( "操作失败" );
    }

    /**
     * 响应返回结果
     *
     * @param res 结果
     *
     * @return 操作结果
     */
    protected RspBase<?> toResult( String res ) {
        return StringUtils.isNotBlank( res ) ? RspBase.ok() : RspBase.businessError( "操作失败" );
    }

    /**
     * 响应返回结果
     *
     * @param isSave 结果
     *
     * @return 操作结果
     */
    protected RspBase<?> toResult( boolean isSave ) {
        return isSave ? RspBase.ok() : RspBase.businessError( "操作失败" );
    }
}
