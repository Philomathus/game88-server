package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 会员银行卡对象 member_card
 *
 * @author mengJun
 */
@Data
public class MemberCard {
    @TableId( value = "id", type = IdType.AUTO )
    private Long    id;
    @Excel( name = "真实姓名" )
    private String  realName;
    @Excel( name = "银行名称" )
    private String  bankName;
    @Excel( name = "银行账号" )
    private String  bankAccount;
    @Excel( name = "银行地址" )
    private String  bankAddress;
    @Excel( name = "会员编号" )
    private String  memberId;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", width = 30, databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date    createTime;
    @Excel( name = "1默认 0不是默认" )
    private Integer dv;
    @Excel( name = "银行卡ID" )
    private Long    bankId;
    @Excel( name = "银行卡归属地" )
    private String  realBankAddress;

    @Override
    public String toString() {
        return new ToStringBuilder( this, ToStringStyle.MULTI_LINE_STYLE )
                .append( "id", getId() )
                .append( "realName", getRealName() )
                .append( "bankName", getBankName() )
                .append( "bankAccount", getBankAccount() )
                .append( "bankAddress", getBankAddress() )
                .append( "memberId", getMemberId() )
                .append( "createTime", getCreateTime() )
                .append( "dv", getDv() ).append( "bankId", getBankId() )
                .append( "realBankAddress", getRealBankAddress() )
                .toString();
    }
}
