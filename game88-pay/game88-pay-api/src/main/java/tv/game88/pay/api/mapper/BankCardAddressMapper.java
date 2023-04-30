package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.BankCardAddress;

import java.util.List;

/**
 * 出款银行地址黑名单Mapper接口
 * list bank card address mapper
 *
 * @author Rajesh
 * @date 2022-12-28
 */
public interface BankCardAddressMapper extends BaseMapper<BankCardAddress> {

    /**
     * 查询出款银行地址黑名单列表
     *list bank card address
     * @param bankCardAddress 出款银行地址黑名单
     * @return 出款银行地址黑名单集合
     */
    List<BankCardAddress> selectBankCardAddressList( BankCardAddress bankCardAddress );

    /**
     * 修改出款银行地址黑名单
     * update bank card address mapper
     * @param bankCardAddress 出款银行地址黑名单
     * @return 结果
     */
    int updateBankCardAddress( BankCardAddress bankCardAddress );
}
