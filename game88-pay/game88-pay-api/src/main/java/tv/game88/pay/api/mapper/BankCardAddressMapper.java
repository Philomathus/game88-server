package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import tv.game88.pay.api.entity.BankCardAddress;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 * list bank card address mapper
 *
 * @author Rajesh
 * @date 2022-12-28
 */
public interface BankCardAddressMapper extends BaseMapper<BankCardAddress> {

    /**
     * 查询【请填写功能名称】列表
     *list bank card address
     * @param bankCardAddress 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    List<BankCardAddress> selectBankCardAddressList( BankCardAddress bankCardAddress );

    /**
     * 修改【请填写功能名称】
     * update bank card address mapper
     * @param bankCardAddress 【请填写功能名称】
     * @return 结果
     */
    int updateBankCardAddress( BankCardAddress bankCardAddress );
}
