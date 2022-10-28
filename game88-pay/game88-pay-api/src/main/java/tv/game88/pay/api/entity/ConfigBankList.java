package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 银行卡列表对象 bank_list
 *
 * @author mengJun
 */
@Data
public class ConfigBankList {
    @Schema( title = "系统编号" )
    @TableId( type = IdType.AUTO )
    private Long   id;
    @Schema( title = "银行编码(选填)" )
    @Excel( name = "银行编码(选填)" )
    private String bankCode;
    @Schema( title = "银行名称" )
    @Excel( name = "银行名称" )
    private String bankName;
    @Schema( title = "银行图标" )
    @Excel( name = "银行图标" )
    private String bankIcon;
    @Schema( title = "状态", description = "1激活 0隐藏" )
    @Excel( name = "状态" )
    private String status;
    @Schema( title = "排序" )
    @Excel( name = "排序" )
    private Long   sort;
    @Schema( title = "开始颜色" )
    @Excel( name = "开始颜色" )
    private String colorStart;
    @Schema( title = "结束颜色" )
    @Excel( name = "结束颜色" )
    private String colorEnd;
}
