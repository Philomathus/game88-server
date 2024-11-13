package tv.game88.pay.app;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.RedisUtils;
import tv.game88.core.config.entity.ConfigBankList;

import jakarta.annotation.Resource;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@SpringBootTest
public class Test {
    @Resource
    private RedisUtils redisUtils;

    @org.junit.jupiter.api.Test
    public void test() {
        List<ConfigBankList> configBankLists = redisUtils.hGetAll( "config:bankList" ).values().stream()
                .map( obj -> JsonUtil.json2Object( obj.toString(), ConfigBankList.class ) ).filter( Objects::nonNull )
                .filter( config -> config.getEffect() != null && config.getEffect() ).toList();
        System.out.println( configBankLists );
    }
}
