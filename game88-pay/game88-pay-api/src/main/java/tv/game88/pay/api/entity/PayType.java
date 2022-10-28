package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 支付类型对象 pay_type
 *
 * @author 77tv
 * @date 2021-01-25
 */
@Data
public class PayType{
    private static final long serialVersionUID = 1L;

    /** 系统编号 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 名称 */
    @Excel(name = "名称")
    private String name;

    /** 编码 */
    @Excel(name = "编码")
    private Integer code;

    /** 图标 */
    @Excel(name = "图标")
    private String iconUrl;

    /** 排序 */
    @Excel(name = "排序")
    private Long indexes;

    /** 是否推荐(1是0否) */
    @Excel(name = "是否推荐(1是0否)")
    private String isRecommend;

    /** 状态(1启用0停用) */
    @Excel(name = "状态(1启用0停用)")
    private String status;

    /** 支付类型 1线上支付 2线下支付 3 代充支付 */
    @Excel(name = "支付类型 1线上支付 2线下支付 3代充支付")
    private String type;

    /** 设备类型 1安卓 2ios */
    @Excel(name = "设备类型 ios,安卓,鸿蒙 以英文逗号分隔")
    private String deviceType;

    /** 开放层级*/
    @Excel(name = "开放层级")
    private Integer openLevelType;

    /** 币种编码 */
    @Excel(name = "币种编码")
    @Schema( title = "币种编码" )
    private String currencyCode;

    /** 创建人 */
    @Excel(name = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date createTime;

    /** 修改人 */
    @Excel(name = "修改人")
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date updateTime;

    @Excel(name = "文本1")
    private String tex1;
    @Excel(name = "文本2")
    private String tex2;
    @Excel(name = "文本3")
    private String tex3;
    @Excel(name = "文本4")
    private String tex4;
    @Excel(name = "文本5")
    private String tex5;

    @Excel(name = "银行编码")
    private String bankCode;

    @Excel(name = "是否展示通道")
    private String isShowChannel;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("name", getName())
                .append("code", getCode())
                .append("iconUrl", getIconUrl())
                .append("indexes", getIndexes())
                .append("isRecommend", getIsRecommend())
                .append("status", getStatus())
                .append("type", getType())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("tex1", getTex1())
                .append("tex2", getTex2())
                .append("tex3", getTex3())
                .append("tex4", getTex4())
                .append("tex5", getTex5())
                .append("openLevelType", getOpenLevelType())
                .toString();
    }
}
