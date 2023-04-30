package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.BankCardAddress;
import tv.game88.pay.api.mapper.BankCardAddressMapper;
import tv.game88.pay.api.service.BankCardAddressService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 出款银行地址黑名单Service业务层处理
 *
 * @author Rajesh
 * @date 2022-12-28
 */
@Service
public class BankCardAddressServiceImpl extends ServiceImpl<BankCardAddressMapper, BankCardAddress> implements BankCardAddressService {

    @Resource
    private BankCardAddressMapper bankCardAddressMapper;

    /**
     * 查询出款银行地址黑名单列表
     * list all bank card address service impl
     * @param bankCardAddress 出款银行地址黑名单
     * @return 出款银行地址黑名单
     */
    @Override
    public List<BankCardAddress> selectBankCardAddressList( BankCardAddress bankCardAddress ) {
        return bankCardAddressMapper.selectBankCardAddressList(bankCardAddress);
    }

    /**
     * 修改出款银行地址黑名单
     * update bank card address service impl
     * @param bankCardAddress 出款银行地址黑名单
     * @return 结果
     */
    @Override
    public int updateBankCardAddress( BankCardAddress bankCardAddress ) {
        return bankCardAddressMapper.updateBankCardAddress(bankCardAddress);
    }
}
