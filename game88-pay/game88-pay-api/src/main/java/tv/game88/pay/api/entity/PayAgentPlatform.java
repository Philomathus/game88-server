package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 代付通道对象 pay_agent_channel
 *
 * @author 77tv
 * @date 2021-01-26
 */
@Data
public class PayAgentPlatform {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 代付平台编码 */
    @Excel(name = "代付平台编码")
    private String platformCode;

    /** 代付平台名称 */
    @Excel(name = "代付平台名称")
    private String platformName;

    /** 创建人 */
    @Excel(name = "创建人")
    private String creator;

    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date createTime;

    /** 修改人 */
    @Excel(name = "修改人")
    private String updator;

    /**
     * 更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "更新时间", isImportField = "true", exportFormat = "yyyy-MM-dd HH:mm:ss",
            importFormat = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date updateTime;

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("platformCode", getPlatformCode())
            .append("platformName", getPlatformName())
            .append("creator", getCreator())
            .append("createTime", getCreateTime())
            .append("updator", getUpdator())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
