package tv.game88.wallet.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigBankListCache;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.dto.RspConfigBankList;
import tv.game88.wallet.api.dto.ReqPayMethod;
import tv.game88.wallet.api.dto.RspPayMethod;
import tv.game88.wallet.api.service.WalletUserPayMethodService;
import tv.game88.wallet.api.type.WalletPayMethodEnum;
import tv.game88.wallet.app.utils.MemberSecurityUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@Tag( name = "支付方式接口" )
@Log4j2
public class PayMethodController extends BaseController {
    @Resource
    private WalletUserPayMethodService walletUserPayMethodService;
    @Resource
    private ConfigBankListCache        configBankListCache;

    @Operation( summary = "获取支付方式类型列表" )
    @PostMapping( "/api/getPayMethodTypes" )
    public RspBase<List<String>> getPayMethodTypes() {
        return RspBase.ok( WalletPayMethodEnum.getPayMethodTypes() );
    }

    @Operation( summary = "获取银行字典列表" )
    @PostMapping( "/api/getBankList" )
    public RspBase<List<RspConfigBankList>> bankList() {
        List<RspConfigBankList> effectList  = configBankListCache.getEffectList();
        String                  domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        for ( RspConfigBankList bankList : effectList ) {
            if ( StringUtils.isNotBlank( bankList.getBankIcon() ) && !bankList.getBankIcon().startsWith( "http" ) ) {
                bankList.setBankIcon( domainValue + bankList.getBankIcon() );
            }
        }
        return RspBase.ok( effectList );
    }

    @Operation( summary = "是否有支付方式" )
    @PostMapping( "/api/hasPayMethod" )
    public RspBase<Boolean> hasPayMethod() {
        return walletUserPayMethodService.hasPayMethod( MemberSecurityUtils.getUserId() );
    }

    @Operation( summary = "绑定新支付方式" )
    @PostMapping( "/api/bindNewPayMethod" )
    public RspBase<?> bindNewPayMethod( @RequestBody @Validated ReqPayMethod reqPayMethod ) {
        return walletUserPayMethodService.bindNewPayMethod( MemberSecurityUtils.getUserId(), reqPayMethod );
    }

    @Operation( summary = "解绑支付方式" )
    @PostMapping( "/api/unBindPayMethod" )
    public RspBase<?> unBindPayMethod( @RequestBody int payMethodId ) {
        return walletUserPayMethodService.unBindPayMethod( MemberSecurityUtils.getUserId(), payMethodId );
    }

    @Operation( summary = "获取支付方式列表" )
    @PostMapping( "/api/getPayMethod" )
    public RspBase<Map<String, List<RspPayMethod>>> getPayMethod() {
        return walletUserPayMethodService.getPayMethod( MemberSecurityUtils.getUserId() );
    }
}
