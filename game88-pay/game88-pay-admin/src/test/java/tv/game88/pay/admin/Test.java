package tv.game88.pay.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tv.game88.core.member.entity.MemberCard;
import tv.game88.core.member.mapper.MemberCardMapper;

import jakarta.annotation.Resource;

@Log4j2
@SpringBootTest( classes = { Game88PayAdminApplication.class } )// 指定启动类
@ActiveProfiles( "8800" )
public class Test {
    @Resource
    private MemberCardMapper memberCardMapper;

    @org.junit.jupiter.api.Test
    public void test(){
        MemberCard memberCard = memberCardMapper.selectOne( new QueryWrapper<MemberCard>()
                .eq( "member_id", "10029" )
                .select( "real_name", "id" )
                .last( "limit 1" ) );
        System.out.println(memberCard);
    }
}
