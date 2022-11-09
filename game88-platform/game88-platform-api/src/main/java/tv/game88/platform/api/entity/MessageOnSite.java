package tv.game88.platform.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageOnSite {
    @TableId( value = "id", type = IdType.AUTO )
    private Long          id;
    @Excel( name = "信息标题" )
    private String        title;
    @Excel( name = "内容" )
    private String        content;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "发布时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime createTime;
    @Excel( name = "发布人" )
    private String        createBy;
}
