package tv.game88.lottery.api.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.common.utils.StringUtils;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.lottery.api.entity.LotteryInfo;
import tv.game88.lottery.api.mapper.LotteryInfoMapper;
import tv.game88.lottery.api.service.LotteryInfoService;

import java.util.List;

/**
 * 彩票信息Service业务层处理
 *
 * @author mengJun
 */
@Service
public class LotteryInfoServiceImpl extends ServiceImpl<LotteryInfoMapper, LotteryInfo> implements LotteryInfoService {
    /**
     * 查询彩票信息列表
     *
     * @param lotteryInfo 彩票信息
     *
     * @return 彩票信息
     */
    @Override
    public List<LotteryInfo> selectLotteryInfoList( LotteryInfo lotteryInfo ) {
        List<LotteryInfo> lotteryInfos = this.baseMapper.selectLotteryInfoList( lotteryInfo );

        String domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        if ( !CollectionUtils.isEmpty( lotteryInfos ) ) {
            for ( LotteryInfo info : lotteryInfos ) {
                if ( StringUtils.isNotBlank( info.getIcon() ) && !info.getIcon().startsWith( "http" ) ) {
                    info.setIcon( domainValue + info.getIcon() );
                }
            }
        }
        return lotteryInfos;
    }
}