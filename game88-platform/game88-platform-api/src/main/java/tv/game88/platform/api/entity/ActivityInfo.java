package tv.game88.platform.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class ActivityInfo {
    /**
     * 系统编号
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 图标
     */
    private String icon;

    /**
     * 标题
     */
    private String title;

    /**
     * 活动类型id
     */
    private Long typeId;

    /**
     * 活动详情
     */
    private String content;

    /**
     * 0停用1启用
     */
    private Boolean effect;

    /**
     * 0=活动详情 1=跳转链接, 3 = webview
     */
    private Integer type;

    /**
     * 图标跳转链接
     */
    private String url;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Map<String , Object> params = new HashMap<>();

    public Map<String,Object> getParams(){
        if(params ==null){
            return new HashMap<>();
        }
        return params;
    }
}