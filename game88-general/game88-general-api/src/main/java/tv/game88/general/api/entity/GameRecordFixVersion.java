package tv.game88.general.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游戏注单版本补单对象 game_record_fix_version
 *
 * @author MengJun
 */
@TableName( "game_record_fix_version" )
@Data
@EqualsAndHashCode( callSuper = false )
public class GameRecordFixVersion {
    @TableId( type = IdType.INPUT )
    private Long   platformId;
    private String versionValue;
}
