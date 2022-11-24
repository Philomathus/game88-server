package tv.game88.game.api.service.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tv.game88.game.api.dto.RspGameDataLog;
import tv.game88.game.api.entity.MemberGameData;
import tv.game88.game.api.mapper.MemberGameDataMapper;
import tv.game88.game.api.service.GameDataService;
import tv.game88.game.api.service.GameService;
import tv.game88.game.api.type.EnumGameCategory;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class GameDataServiceImpl implements GameDataService {
    @Resource
    private GameService        gameService;
    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public void beatGameCodeAgent( String dTime, String start, String end, String account, EnumGameCategory gameCategory ) {
        List<RspGameDataLog> rspGameDataLogs = gameService.remoteDataGrab( start, end, account,
                gameCategory != null ? EnumGameCategory.getDataRemoteByEnum( gameCategory ) : null );
        if ( CollectionUtils.isEmpty( rspGameDataLogs ) ) {
            return;
        }
        Map<String, BigDecimal> willCodeMap  = new HashMap<>();
        List<MemberGameData>    willCodeList = new ArrayList<>();
        SqlSession              session      = sqlSessionTemplate.getSqlSessionFactory().openSession( ExecutorType.BATCH, false );
        MemberGameDataMapper    mapper       = session.getMapper( MemberGameDataMapper.class );
        for ( RspGameDataLog dataLog : rspGameDataLogs ) {
            if (mapper.findExist(dataLog.getAccount().substring(dataLog.getAccount().length() - 1), dataLog.getId()) != null) {
                continue;
            }
        }
    }


}
