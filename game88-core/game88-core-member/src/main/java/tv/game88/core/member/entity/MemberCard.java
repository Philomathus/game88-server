package tv.game88.core.member.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

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
    @Excel( name = "会员编号" )
    private String  memberId;
    @Excel( name = "银行卡ID" )
    private Long    bankId;
    @Excel( name = "真实姓名" )
    private String  realName;
    @Excel( name = "银行账号" )
    private String  bankAccount;
    @Excel( name = "开户地址(选填)" )
    private String  bankAddress;
    @Excel( name = "是否默认" )
    private boolean dv;
    @Excel( name = "银行卡归属地" )
    private String  realBankAddress;

    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    @Excel( name = "创建时间", databaseFormat = "yyyy-MM-dd HH:mm:ss" )
    private Date createTime;
}
