package tv.game88.platform.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName( "mobile_limit" )
@Data
@EqualsAndHashCode( callSuper = false )
public class MobileLimit {
    /**
     * 限制手机号
     */
    @TableId( type = IdType.INPUT )
    private String mobile;
}