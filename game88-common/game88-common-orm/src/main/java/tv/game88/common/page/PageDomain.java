package tv.game88.common.page;

import io.swagger.annotations.ApiModelProperty;
import tv.game88.common.utils.StringUtils;

/**
 * 分页数据
 *
 * @author MengJun
 */
public class PageDomain {
    /**
     * 当前记录起始索引
     */
    @ApiModelProperty( value = "当前页", example = "1" )
    private Integer pageNum = 1;

    /**
     * 每页显示记录数
     */
    @ApiModelProperty( value = "每页条数", example = "10" )
    private Integer pageSize = 10;

    /**
     * 排序列
     */
    @ApiModelProperty( hidden = true )
    private String orderByColumn;

    /**
     * 排序的方向desc或者asc
     */
    @ApiModelProperty( hidden = true )
    private String isAsc = StringUtils.EMPTY;

    public String getOrderBy() {
        if ( StringUtils.isBlank( orderByColumn ) ) {
            return StringUtils.EMPTY;
        }
        return StringUtils.toUnderScoreCase( orderByColumn ) + StringUtils.SPACE
                + ( StringUtils.isBlank( isAsc ) ? StringUtils.EMPTY : isAsc );
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum( Integer pageNum ) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize( Integer pageSize ) {
        if ( pageSize == null ) {
            pageSize = 0;
        }
        this.pageSize = pageSize;
    }

    public String getOrderByColumn() {
        return orderByColumn;
    }

    public void setOrderByColumn( String orderByColumn ) {
        this.orderByColumn = orderByColumn;
    }

    public String getIsAsc() {
        return isAsc;
    }

    public void setIsAsc( String isAsc ) {
        this.isAsc = isAsc;
    }
}
