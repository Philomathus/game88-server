package tv.game88.pay.app;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.boot.test.context.SpringBootTest;
import tv.game88.pay.api.entity.MemberWithdrawDetail;
import tv.game88.pay.api.mapper.MemberWithdrawDetailMapper;

import jakarta.annotation.Resource;

@SpringBootTest
public class Test {
    @Resource
    private MemberWithdrawDetailMapper memberWithdrawDetailMapper;

    @org.junit.jupiter.api.Test
    public void test() {
        Long aLong = this.memberWithdrawDetailMapper.selectCount( new QueryWrapper<MemberWithdrawDetail>()
                .eq( "withdraw_id", "1001" )
                .in( "status", 3, 6 ) );
        System.out.println(aLong <= 0);
    }
}
