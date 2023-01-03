package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import tv.game88.pay.api.entity.BankCardAddress;
import tv.game88.pay.api.mapper.BankCardAddressMapper;
import tv.game88.pay.api.service.BankCardAddressService;

import javax.annotation.Resource;
import java.util.List;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author Rajesh
 * @date 2022-12-28
 */
@Service
public class BankCardAddressServiceImpl extends ServiceImpl<BankCardAddressMapper, BankCardAddress> implements BankCardAddressService {

    @Resource
    private BankCardAddressMapper bankCardAddressMapper;

    /**
     * 查询【请填写功能名称】列表
     * list all bank card address service impl
     * @param bankCardAddress 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<BankCardAddress> selectBankCardAddressList( BankCardAddress bankCardAddress ) {
        return bankCardAddressMapper.selectBankCardAddressList(bankCardAddress);
    }

    /**
     * 修改【请填写功能名称】
     * update bank card address service impl
     * @param bankCardAddress 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int updateBankCardAddress( BankCardAddress bankCardAddress ) {
        return bankCardAddressMapper.updateBankCardAddress(bankCardAddress);
    }
}
