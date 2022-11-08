package tv.game88.pay.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.vo.RspBase;
import tv.game88.pay.api.cache.ConfigBankListCache;
import tv.game88.pay.api.dto.RspConfigBankList;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "银行字典列表相关接口" )
@Log4j2
public class ConfigBankListController {
    @Resource
    private ConfigBankListCache configBankListCache;

    @Operation( summary = "获取银行字典列表" )
    @PostMapping( "/bankList" )
    public RspBase<List<RspConfigBankList>> bankList() {
        return RspBase.ok( configBankListCache.getEffectList() );
    }
}
