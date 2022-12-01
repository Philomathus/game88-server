package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 打码对象 member_bcode
 *
 * @author mengJun
 */
@Data
public class MemberBcode {
    @TableId( value = "id", type = IdType.AUTO )
    private String        id;
    @Excel( name = "会员账号ID" )
    private String        userId;
    @Excel( name = "描述" )
    private String        des;
    @Excel( name = "充值金额" )
    private BigDecimal    charge;
    @Excel( name = "收入" )
    private BigDecimal    income;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Excel( name = "状态" )
    // 0=未打码 1=已打码
    private Integer       status;
    @Excel( name = "当前打码量" )
    private BigDecimal    cur;

    @TableField( exist = false )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String[]   selectDate;
    @TableField( exist = false )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String     startTime;
    @TableField( exist = false )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private String     endTime;
    @TableField( exist = false )
    private BigDecimal total;
    @TableField( exist = false )
    private BigDecimal countCur;

    @TableField( exist = false )
    @JsonProperty( access = JsonProperty.Access.WRITE_ONLY )
    private Integer googleAuthCode;


    /**
     * 请求参数
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField(exist = false)
    private Map<String, Object> params;

    public Map<String, Object> getParams() {
        if ( params == null ) {
            params = new HashMap<>();
        }
        return params;
    }
}
