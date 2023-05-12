package tv.game88.general.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游戏平台对象 game_platform
 *
 * @author MengJun
 */
@TableName( "game_record_version" )
@Data
@EqualsAndHashCode( callSuper = false )
public class GameRecordVersion {
    @TableId( type = IdType.INPUT )
    private Long   platformId;
    private String versionValue;
}
