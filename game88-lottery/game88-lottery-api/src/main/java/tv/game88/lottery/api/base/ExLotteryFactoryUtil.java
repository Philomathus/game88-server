package tv.game88.lottery.api.base;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class ExLotteryFactoryUtil {
    @Resource
    private ApplicationContext context;

    public ExLottery createExProcessor( Integer kindId ) {
        return ( ExLottery ) context.getBean( "Ex" + kindId + "Processor" );
    }
}
