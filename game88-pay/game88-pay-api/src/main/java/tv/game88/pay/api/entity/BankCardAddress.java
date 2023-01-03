package tv.game88.pay.api.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 【请填写功能名称】对象 bank_card_address
 *
 * @author Rajesh
 * @date 2022-12-28
 */
@Data
public class BankCardAddress {

    /** 系统编号 */
    @TableId( type = IdType.AUTO )
    private String id;

    /** 姓名 */
    @Excel(name = "姓名")
    private String province;

    /** 银行名称 */
    @Excel(name = "银行名称")
    private String city;

    /** 0禁用1启用 */
    @Excel(name = "0禁用1启用")
    private String status;

    /** 创建人 */
    @Excel(name = "创建人")
    private String createName;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("province", getProvince())
                .append("city", getCity())
                .append("status", getStatus())
                .append("createName", getCreateName())
                .toString();
    }


}
