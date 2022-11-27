package tv.game88.pay.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.pay.api.dto.RspPayRechargeUsdt;
import tv.game88.pay.api.entity.PayRechargeUsdt;
import tv.game88.pay.api.mapper.PayRechargeUsdtMapper;
import tv.game88.pay.api.service.PayRechargeUsdtService;

import java.util.Collections;
import java.util.List;

@Service
public class PayRechargeUsdtServiceImpl extends ServiceImpl<PayRechargeUsdtMapper, PayRechargeUsdt> implements PayRechargeUsdtService {
    @Override
    public List<PayRechargeUsdt> selectPayRechargeUsdtList( PayRechargeUsdt payRechargeUsdt ) {
        return this.baseMapper.selectPayRechargeUsdtList( payRechargeUsdt );
    }

    @Override
    public List<RspPayRechargeUsdt> selectList( String memberId, Integer vip ) {
        List<RspPayRechargeUsdt> resultList = this.baseMapper.selectEffectRspList();
        if ( !CollectionUtils.isEmpty( resultList ) ) {
            Collections.shuffle( resultList );
        }
        return resultList;
    }
}

