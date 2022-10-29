package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

/**
 * 支付类型对象 pay_type
 *
 * @author mengJun
 */
@Data
public class PayType {
    @TableId( type = IdType.AUTO )
    private Long          id;
    @Excel( name = "名称" )
    private String        name;
    @Excel( name = "图标" )
    private String        iconUrl;
    @Excel( name = "排序" )
    private Long          indexes;
    @Excel( name = "是否推荐" )
    private boolean       recommend;
    @Excel( name = "状态" )
    private Boolean       effect = false;
    // 1线上支付 2线下支付 3 代充支付 4 USDT
    @Excel( name = "支付类型" )
    private String        type;
    // ios,安卓 以英文逗号分隔
    @Excel( name = "设备类型" )
    private String        deviceType;
    @Excel( name = "开放层级-最小" )
    private Integer       openLevelMin;
    @Excel( name = "开放层级-最大" )
    private Integer       openLevelMax;
    @Excel( name = "创建人" )
    private String        createBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Excel( name = "修改人" )
    private String        updateBy;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;

    @Excel( name = "文本1" )
    private String tex1;
    @Excel( name = "文本2" )
    private String tex2;
    @Excel( name = "文本3" )
    private String tex3;
    @Excel( name = "文本4" )
    private String tex4;
    @Excel( name = "文本5" )
    private String tex5;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "id", getId() )
                .append( "name", getName() )
                .append( "iconUrl", getIconUrl() )
                .append( "indexes", getIndexes() )
                .append( "recommend", isRecommend() )
                .append( "effect", isEffect() )
                .append( "type", getType() )
                .append( "createBy", getCreateBy() )
                .append( "createTime", getCreateTime() )
                .append( "updateBy", getUpdateBy() )
                .append( "updateTime", getUpdateTime() )
                .append( "tex1", getTex1() )
                .append( "tex2", getTex2() )
                .append( "tex3", getTex3() )
                .append( "tex4", getTex4() )
                .append( "tex5", getTex5() )
                .append( "openLevelMin", getOpenLevelMin() )
                .append( "openLevelMax", getOpenLevelMax() )
                .toString();
    }
}
