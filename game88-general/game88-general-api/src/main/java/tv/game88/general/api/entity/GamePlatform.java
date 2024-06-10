package tv.game88.general.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tv.game88.common.utils.AESCoder;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.game.type.EnumGameCategory;

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
    @TableId( type = IdType.INPUT )
    private Long             id;
    /**
     * 游戏类别
     */
    private EnumGameCategory gameCategory;
    /**
     * 平台名称
     */
    private String           name;
    /**
     * 代理（渠道）号
     */
    private String           agent;
    /**
     * API接口
     */
    private String           apiUrl;
    /**
     * 查询注单
     */
    private String           recordUrl;
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
    private String           linecode;
    /**
     * 激活状态(1启用0停用)
     */
    private Boolean          effect;
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

    @TableField( exist = false )
    private String versionValue;

    private boolean isFix = false;

    public String getDes() {
        if ( StringUtils.isNotBlank( des ) ) {
            try {
                return AESCoder.decrypt( des );
            } catch ( Exception ig ) {
                //
            }
        }
        return des;
    }

    public String getMd5() {
        if ( StringUtils.isNotBlank( md5 ) ) {
            try {
                return AESCoder.decrypt( md5 );
            } catch ( Exception ig ) {
                //
            }
        }
        return md5;
    }

    public String getMd5Original() {
        return md5;
    }

    public String getDesOriginal() {
        return des;
    }
}