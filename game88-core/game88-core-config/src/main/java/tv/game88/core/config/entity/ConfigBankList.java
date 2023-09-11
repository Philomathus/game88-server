package tv.game88.core.config.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 银行卡列表对象 bank_list
 *
 * @author mengJun
 */
@Data
@NoArgsConstructor
public class ConfigBankList {
    @TableId( type = IdType.AUTO )
    private Long    id;
    // ( name = "银行编码(选填)" )
    private String  bankCode;
    // ( name = "银行名称" )
    private String  bankName;
    // ( name = "银行图标" )
    private String  bankIcon;
    // ( name = "激活状态" )
    private Boolean effect;
    // ( name = "排序" )
    private Long    sort;
}
