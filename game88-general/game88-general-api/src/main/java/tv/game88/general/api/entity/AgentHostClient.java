package tv.game88.general.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode( callSuper = false )
public class AgentHostClient {
    @TableId( type = IdType.AUTO )
    private Integer id;
    /**
     * 设备号（1=ios 2=安卓）
     */
    private String dev;
    /**
     * 版本号
     */
    private Integer version;
    /**
     * 版本号名称
     */
    private String versionName;
    /**
     * 1=强更0=不强更
     */
    private Integer latestFore;
    /**
     * 下载地址
     */
    private String url;
    /**
     * 更新内容
     */
    private String updateText;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    /**
     * 最后更新人
     */
    private String updateBy;
    /**
     * 最后更新时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime updateTime;

}
