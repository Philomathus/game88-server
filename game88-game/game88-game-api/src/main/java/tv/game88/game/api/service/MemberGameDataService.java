package tv.game88.game.api.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tv.game88.common.vo.RspBase;
import tv.game88.game.api.dto.*;
import tv.game88.game.api.entity.LogCleanCodeInfo;
import tv.game88.game.api.entity.MemberGameData;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 会员游戏注单数据Service接口
 *
 * @author mengJun
 */
public interface MemberGameDataService extends IService<MemberGameData> {
    /**
     * 查询会员游戏注单数据列表
     *
     * @return 会员游戏注单数据集合
     */
    public List<MemberGameData> selectMemberGameDataList( ReqMemberGameData reqMemberGameData );

    public void opCleanCode( String memberId, RspCleanCodeResult restlt, Collection<LogCleanCodeInfo> logCleanCodeInfos,
                             String cleanId, LocalDateTime ntime );

    List<RspCleanCodeLog> cleanCodeLogs( String memberId );

    List<RspGameData> getGameDataList( String memberId, ReqGameData reqGameData );

    MemberGameData getCount( ReqMemberGameData reqMemberGameData );

    RspBase<?> getGameBetRecordData( MemberGameData memberGameData );

    RspBase<?> getGameBetDetailData( MemberGameData memberGameData );
}