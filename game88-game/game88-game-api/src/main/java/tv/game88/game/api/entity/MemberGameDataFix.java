package tv.game88.game.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberGameDataFix {
    @TableId( type = IdType.AUTO )
    private Integer       id;
    /**
     * 账号
     */
    @Excel( name = "账号" )
    private String        memberId;
    /**
     * 游戏开始时间
     */
    @Excel( name = "游戏开始时间" )
    private LocalDateTime gameStartTime;
    /**
     * 游戏结束时间
     */
    @Excel( name = "游戏结束时间" )
    private LocalDateTime gameEndTime;
    /**
     * 本地平台id
     */
    @Excel( name = "本地平台id" )
    private Integer       platformId;
    /**
     * 0:未处理1已处理
     */
    @Excel( name = "0:未处理1已处理" )
    private Integer       status;

    private String platformName;
}