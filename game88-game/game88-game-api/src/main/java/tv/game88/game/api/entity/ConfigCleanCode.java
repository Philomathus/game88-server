package tv.game88.game.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 洗码配置对象 config_clean_code
 *
 * @author krzystof
 */
@TableName( "config_clean_code" )
@Data
@EqualsAndHashCode( callSuper = false )
public class ConfigCleanCode {

    /**
     * 系统编号
     */
    @TableId( type = IdType.AUTO )
    private Integer id;

    /**
     * 游戏类型ID
     */
    @Excel( name = "游戏类型ID" )
    private Long typeId;

    /**
     * 最小打码
     */
    @Excel( name = "最小打码" )
    private BigDecimal codeMin;

    /**
     * 最大打码
     */
    @Excel( name = "最大打码" )
    private BigDecimal codeMax;

    /**
     * 洗码比例
     */
    @Excel( name = "洗码比例" )
    private BigDecimal cleanCodeRate;

    /**
     * 激活状态
     */
    @Excel( name = "激活状态" )
    private Boolean effect;
}
