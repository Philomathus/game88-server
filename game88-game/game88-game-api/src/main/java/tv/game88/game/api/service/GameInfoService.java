package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.game.api.dto.ReqGameTypeWith;
import tv.game88.game.api.entity.GameInfo;

import java.util.List;

/**
 * 游戏信息Service接口
 *
 * @author mengJun
 */
public interface GameInfoService extends IService<GameInfo> {
    /**
     * 查询游戏信息列表
     *
     * @param gameInfo 游戏信息
     *
     * @return 游戏信息集合
     */
    public List<GameInfo> selectGameInfoList( GameInfo gameInfo );

    List<GameInfo> selectListByType( Long typeId );

    List<GameInfo> selectListNotType( Long typeId );

    RspBase<?> editTypeWith( Long typeId, List<ReqGameTypeWith> reqGameTypeWiths );
}