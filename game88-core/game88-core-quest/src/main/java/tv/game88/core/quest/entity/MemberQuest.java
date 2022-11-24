package tv.game88.core.quest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author qicheng
 * @since 2021-10-04
 */
@Data
public class MemberQuest {
    @Schema( title = "系统编号" )
    @TableId( type = IdType.INPUT )
    private String  id;
    @Schema( title = "会员id" )
    private String  memberId;
    @Schema( title = "任务id" )
    private Long    questId;
    @Schema( title = "0=进行中1=已经完成2 领奖完成" )
    private Integer status;
    @Schema( title = "当前任务数量" )
    private Integer curNum;
    @Schema( title = "任务模型" )
    private Integer taskMode;
}
