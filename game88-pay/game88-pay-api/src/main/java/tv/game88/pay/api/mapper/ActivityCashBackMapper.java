package tv.game88.pay.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import tv.game88.pay.api.entity.ActivityCashBack;

import java.math.BigDecimal;
import java.util.List;

/**
 * 【返现活动】Mapper接口
 *
 * @author 77tv
 * @date 2021-06-07
 */
public interface ActivityCashBackMapper extends BaseMapper<ActivityCashBack> {
    /**
     * 查询【返现活动】列表
     *
     * @param activityCashBack 【返现活动】
     *
     * @return 【返现活动】集合
     */
    public List<ActivityCashBack> selectActivityCashBackList( ActivityCashBack activityCashBack );

    Integer selectActivityCashBackBycash( @Param( "cash" ) BigDecimal cash );

}
