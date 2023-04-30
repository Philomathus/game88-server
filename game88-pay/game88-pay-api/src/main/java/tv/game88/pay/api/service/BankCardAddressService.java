package tv.game88.pay.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.pay.api.entity.BankCardAddress;

import java.util.List;

/**
 * 出款银行地址黑名单Service接口
 *
 * @author Rajesh
 * @date 2022-12-28
 */
public interface BankCardAddressService extends IService<BankCardAddress> {

    /**
     * 查询出款银行地址黑名单列表
     *
     * @param bankCardAddress 出款银行地址黑名单
     * @return 出款银行地址黑名单集合
     */
    List<BankCardAddress> selectBankCardAddressList( BankCardAddress bankCardAddress );

    /**
     * 修改出款银行地址黑名单
     * update bank card address service
     * @param bankCardAddress 出款银行地址黑名单
     * @return 结果
     */
   int updateBankCardAddress( BankCardAddress bankCardAddress );
}
