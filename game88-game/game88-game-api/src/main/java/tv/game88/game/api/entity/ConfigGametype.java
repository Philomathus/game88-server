package tv.game88.game.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对象 config_gametype
 *
 * @author MengJun
 */
@TableName( "config_gametype" )
@Data
@EqualsAndHashCode( callSuper = false )
public class ConfigGametype {

    /**
     * 主键
     */
    private String id;

    /**
     * 平台id
     */
    @Excel( name = "平台id" )
    private String platformId;

    /**
     * 平台名称
     */
    @Excel( name = "平台名称" )
    private String platformName;

    /**
     * 子平台id
     */
    @Excel( name = "子平台id" )
    private String sonPlatformId;

    /**
     * 子平台名称
     */
    @Excel( name = "子平台名称" )
    private String sonPlatformName;
}