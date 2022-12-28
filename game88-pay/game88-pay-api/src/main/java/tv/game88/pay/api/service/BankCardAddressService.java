package tv.game88.pay.api.service;


import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.pay.api.entity.BankCardAddress;

import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author Rajesh
 * @date 2022-12-28
 */
public interface BankCardAddressService extends IService<BankCardAddress> {

    /**
     * 查询【请填写功能名称】列表
     *
     * @param bankCardAddress 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    List<BankCardAddress> selectBankCardAddressList( BankCardAddress bankCardAddress );

    /**
     * 修改【请填写功能名称】
     * update bank card address service
     * @param bankCardAddress 【请填写功能名称】
     * @return 结果
     */
   int updateBankCardAddress( BankCardAddress bankCardAddress );
}
