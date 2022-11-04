package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 支付类型对象 pay_type
 *
 * @author mengJun
 */
@Data
@NoArgsConstructor
public class PayType {
    @TableId( type = IdType.AUTO )
    private Long          id;
    @Excel( name = "名称" )
    private String        name;
    @Excel( name = "图标" )
    private String        iconUrl;
    @Excel( name = "排序" )
    private Integer       sort;
    @Excel( name = "是否推荐" )
    private Boolean       recommend;
    @Excel( name = "激活状态" )
    private Boolean       effect;
    // 支付类型 1线上支付 2线下支付 3 代充支付 4 USDT
    @Excel( name = "支付类型" )
    private Integer       type;
    // 设备类型 1 ios 2 android 以英文逗号分隔
    @Excel( name = "支持设备类型" )
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
    @Excel( name = "修改时间", format = "yyyy-MM-dd HH:mm:ss", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
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
}
