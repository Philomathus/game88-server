package tv.game88.lottery.api.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tv.game88.lottery.api.entity.LotteryGame;

import java.util.List;

/**
 * 彩票下注配置Mapper接口
 *
 * @author mengJun
 */
public interface LotteryGameMapper extends BaseMapper<LotteryGame> {

    /**
     * 查询下注列表
     *
     * @param lotteryGame 下注
     *
     * @return 下注集合
     */
    List<LotteryGame> selectLotteryGameList( LotteryGame lotteryGame );

    @Update( "update lottery_killrate_liuhecai_one set tema_shengxiao = #{animal} where code_id in ( ${codeId} )" )
    int updateKillrateLiuheOne( @Param( "codeId" ) String codeId, @Param( "animal" ) String animal );
}