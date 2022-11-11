package tv.game88.game.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 游戏类型与游戏信息关联对象 game_type_with
 *
 * @author mengJun
 */
@TableName( "game_type_with" )
@Data
@EqualsAndHashCode( callSuper = false )
public class GameTypeWith {
    /**
     * 类型ID
     */
    private Long typeId;
    /**
     * 游戏ID
     */
    private Long gameInfoId;
    /**
     * 排序号
     */
    private Long sort;
}