package tv.game88.core.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 对象 config_environment
 *
 * @author MengJun
 */
@Data
public class ConfigEnvironment {
    /**
     * 参数标题
     */
    private String envTitle;

    /**
     * 参数编码
     */
    @TableId( value = "env_code", type = IdType.INPUT )
    private String envCode;

    /**
     * 参数值
     */
    private String envValue;

    /**
     * 参数说明
     */
    private String envDes;

    /**
     * 参数组
     */
    private Long envGroup;

    /**
     * 排序
     */
    private Long envSort;

    /**
     * 状态 1启用 0禁用
     */
    private Integer envStatus;

    /**
     * 菜单类型 M是类型C是属性
     */
    @TableField( exist = false )
    private String menuType;
}
