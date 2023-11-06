package tv.game88.pay.api.base;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tv.game88.pay.api.constants.ConstantsPayAgent;

import jakarta.annotation.Resource;

@Component
public class PayAgentProcessorFactoryUtil {
    @Resource
    private ApplicationContext context;

    public BasePayAgent createPayProcessor( String type ) {
        return ( BasePayAgent ) context.getBean( type + ConstantsPayAgent.PROCESSOR );
    }
}
