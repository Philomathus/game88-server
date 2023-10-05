package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.vo.RspBase;
import tv.game88.wallet.api.dto.RspMessage;
import tv.game88.wallet.api.service.WalletMessageService;
import tv.game88.wallet.app.utils.MemberSecurityUtils;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Tag( name = "消息接口" )
@Log4j2
public class MessageController {
    @Resource
    private WalletMessageService walletMessageService;

    @Operation( summary = "获取站内信息列表" )
    @PostMapping( "/api/getMessageList" )
    public RspBase<List<RspMessage>> getMessageList() {
        return RspBase.ok( walletMessageService.getMessageList( MemberSecurityUtils.getUserId() ) );
    }

    @Operation( summary = "设置消息已读" )
    @PostMapping( "/api/setMessageRead" )
    public RspBase<?> setMessageRead( @RequestBody Long messageId ) {
        return walletMessageService.setMessageRead( MemberSecurityUtils.getUserId(), messageId );
    }
}
