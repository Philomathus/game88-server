package tv.game88.lottery.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.vo.RspBase;
import tv.game88.lottery.api.dto.ReqLottery;
import tv.game88.lottery.api.dto.RspLotteryInit;
import tv.game88.lottery.api.service.LotteryService;

import javax.annotation.Resource;

@RestController
@Tag( name = "彩票相关接口" )
@Log4j2
public class LotteryController {
    @Resource
    private LotteryService lotteryService;

    @Operation( summary = "初始化彩票信息" )
    @PostMapping( "init" )
    public RspBase<RspLotteryInit> init( @RequestBody ReqLottery req ) {
        return lotteryService.getRspLotteryInit( req.getId() );
    }
}
