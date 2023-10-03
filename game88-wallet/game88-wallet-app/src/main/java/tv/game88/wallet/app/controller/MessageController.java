package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.wallet.api.service.WalletMessageService;

import javax.annotation.Resource;

@RestController
@Tag( name = "消息接口" )
@Log4j2
public class MessageController {
    @Resource
    private WalletMessageService walletMessageService;
}
