package tv.game88.game.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.game.api.type.EnumGameCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 游戏平台对象 game_platform
 *
 * @author MengJun
 */
@TableName( "game_platform" )
@Data
@EqualsAndHashCode( callSuper = false )
public class GamePlatform {
    /**
     * 主键
     */
    @TableId( type = IdType.AUTO )
    private Long             id;
    /**
     * 游戏类别
     */
    @Excel( name = "游戏类别" )
    private EnumGameCategory gameCategory;
    /**
     * 平台名称
     */
    @Excel( name = "平台名称" )
    private String           name;
    /**
     * 代理（渠道）号
     */
    @Excel( name = "代理" )
    private String           agent;
    /**
     * API接口
     */
    @Excel( name = "API接口" )
    private String           apiUrl;
    /**
     * 查询注单
     */
    @Excel( name = "查询注单" )
    private String           recordUrl;

    /**
     * 小图标
     */
    @Excel( name = "小图标" )
    private String icon;

    /**
     * 卡片图标
     */
    @Excel( name = "卡片图标" )
    private String cardIcon;

    /**
     * DES密钥
     */
    private String           des;
    /**
     * MD5密钥
     */
    private String           md5;
    /**
     * 站点标识
     */
    @Excel( name = "站点标识" )
    private String           linecode;

    /**
     * 洗码比例
     */
    @Excel( name = "洗码比例" )
    private BigDecimal       rateClean;

    /**
     * 打码比例
     */
    @Excel( name = "打码比例" )
    private BigDecimal       rateBeat;
    /**
     * 激活状态(1启用0停用)
     */
    @Excel( name = "激活状态(1启用0停用)" )
    private Boolean          effect;

    /**
     * 排序号
     */
    @Excel( name = "排序号" )
    private int sort;

    /**
     * 是否维护(1是0否)
     */
    @Excel( name = "是否维护(1是0否)" )
    private Boolean          maintain;
    /**
     * 创建者
     */
    private String           createBy;
    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime    createTime;
    /**
     * 更新者
     */
    private String           updateBy;
    /**
     * 更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime    updateTime;
}