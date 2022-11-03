package tv.game88.pay.api.base;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PayAgentProcessorFactoryUtil {
	@Resource
	private ApplicationContext context;

	public BasePayAgent createPayProcessor( String type ) {
		return ( BasePayAgent ) context.getBean( type + "PayAgentProcessor" );
	}
}
