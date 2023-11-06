package tv.game88.pay.api.base;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class PayProcessorFactoryUtil {
	@Resource
	private ApplicationContext context;

	public BasePay createPayProcessor( String type ) {
		return (BasePay) context.getBean( type + "Processor" );
	}
}
