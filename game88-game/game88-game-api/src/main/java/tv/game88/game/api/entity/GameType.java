package tv.game88.game.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 游戏类型对象 game_type
 *
 * @author mengJun
 */
@TableName( "game_type" )
@Data
@EqualsAndHashCode( callSuper = false )
public class GameType {
    /**
     * 系统编号
     */
    @TableId( type = IdType.AUTO )
    private Long id;

    /**
     * 名称
     */
    @Excel( name = "名称" )
    private String name;

    /**
     * 激活状态(1启用0停用)
     */
    @Excel( name = "激活状态(1启用0停用)" )
    private Boolean effect;

    /**
     * 图标
     */
    @Excel( name = "图标" )
    private String icon;

    /**
     * 洗码比例
     */
    @Excel( name = "洗码比例" )
    private BigDecimal      rateClean;

    /**
     * 排序号
     */
    @Excel( name = "排序号" )
    private Long sort;

    /**
     * 显示类型
     */
    @Excel( name = "显示类型")
    private Integer type;
}