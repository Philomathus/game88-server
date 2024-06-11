package tv.game88.platform.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.platform.api.entity.CustomerService;
import tv.game88.platform.api.mapper.CustomerServiceMapper;
import tv.game88.platform.api.service.CustomerServiceService;

import java.util.List;

@Service
public class CustomerServiceServiceImpl extends ServiceImpl<CustomerServiceMapper, CustomerService> implements CustomerServiceService {

    @Override
    public List<CustomerService> selectCustomerServiceList(CustomerService customerService) {
        var queryWrapper = new QueryWrapper<CustomerService>();
        addEqCondition(queryWrapper, "id",customerService.getId());
        addEqCondition(queryWrapper, "title",customerService.getTitle());
        addEqCondition(queryWrapper, "status",customerService.getStatus());
        addEqCondition(queryWrapper, "details",customerService.getDetails());

        List<CustomerService> customerServices = this.baseMapper.selectList(queryWrapper);

        String domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        if ( !CollectionUtils.isEmpty( customerServices ) ) {
            for ( CustomerService cs : customerServices ) {
                if ( StringUtils.isNotBlank( cs.getIcon() ) && !cs.getIcon().startsWith( "http" ) ) {
                    cs.setIcon( domainValue + cs.getIcon() );
                }
            }
        }
        return customerServices;
    }

    /**
     * Add equal condition to queryWrapper is the object value is not empty.
     * @param queryWrapper - the queryWrapper where to add the condition
     * @param column - column to apply the condition
     * @param value - the value to check
     */
    private void addEqCondition(final QueryWrapper<CustomerService> queryWrapper, final String column, final Object value){
        if(ObjectUtils.isNotEmpty(value)) {
            queryWrapper.eq(column, value);
        }
    }
}