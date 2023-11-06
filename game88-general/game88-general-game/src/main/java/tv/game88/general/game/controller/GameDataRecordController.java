package tv.game88.general.game.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.core.game.dto.RspGameDataLog;
import tv.game88.general.api.dto.ReqGameDataRecord;
import tv.game88.general.api.service.GameDataRecordService;

import jakarta.annotation.Resource;
import java.util.List;

@Log4j2
@RestController
@RequestMapping( "/gameDataRecord" )
public class GameDataRecordController {
    @Resource
    private GameDataRecordService gameDataRecordService;

    @PostMapping( value = "getList" )
    public List<RspGameDataLog> getList( @RequestBody ReqGameDataRecord req ) {
        return gameDataRecordService.getListByReq( req );
    }
}
