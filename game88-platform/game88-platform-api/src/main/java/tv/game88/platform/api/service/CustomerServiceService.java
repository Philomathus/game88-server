package tv.game88.platform.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.platform.api.entity.CustomerService;

import java.util.List;

public interface CustomerServiceService extends IService<CustomerService> {

	List<CustomerService> selectCustomerServiceList(CustomerService customerService);
}