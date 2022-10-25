package tv.game88.platform.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.vo.RspBase;
import tv.game88.platform.api.dto.RspInit;
import tv.game88.platform.api.dto.RspManUpdateVersion;
import tv.game88.platform.api.service.MemberInfoService;

import javax.annotation.Resource;

@RestController
@Tag( name = "登录和初始化接口" )
@Log4j2
public class LoginController {
    @Resource
    private MemberInfoService memberInfoService;

    @Operation( summary = "初始化接口", description = "初始化接口" )
    @PostMapping( "/init" )
    public RspBase<RspInit> loginInit( @RequestHeader( "dev" ) Integer dev, @RequestHeader( "version" ) String version ) {
        if ( dev == null || version == null ) {
            return RspBase.businessError( "客户端版本较低" );
        }
        return RspBase.ok( memberInfoService.getLoginInit( dev, version ) );
    }

    @Operation( summary = "人工更新请求版本", description = "人工更新请求版本" )
    @PostMapping( "/check-update" )
    public RspBase<RspManUpdateVersion> getManUpdateVersion( @RequestHeader( "dev" ) Integer dev,
                                                             @RequestHeader( "version" ) String version ) {
        return RspBase.ok( memberInfoService.checkManUpdateVersion( dev, version ) );
    }
}
