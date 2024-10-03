package tv.game88.pay.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.common.base.BaseController;
import tv.game88.common.utils.StringUtils;
import tv.game88.common.vo.RspBase;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.core.config.cache.ConfigBankListCache;
import tv.game88.core.config.dto.RspConfigBankList;

import jakarta.annotation.Resource;
import tv.game88.core.config.entity.ConfigBankList;

import java.util.Arrays;
import java.util.List;

@RestController
@Tag( name = "银行字典列表相关接口" )
@Log4j2
public class ConfigBankListController extends BaseController {
    @Resource
    private ConfigBankListCache configBankListCache;

    @Operation( summary = "获取银行字典列表" )
    @PostMapping( "/bankList" )
    public RspBase<List<RspConfigBankList>> bankList() {
        List<ConfigBankList> effectList = configBankListCache.getEffectList();
        effectList.removeIf( r -> Arrays.asList( "GOPAY", "OKPAY", "VIPPAY" ).contains( r.getBankName().toUpperCase() ) );
        String domainValue = ConfigDomainCacheUtil.me.getDomainOssValue();
        for ( ConfigBankList bankList : effectList ) {
            if ( StringUtils.isNotBlank( bankList.getBankIcon() ) && !bankList.getBankIcon().startsWith( "http" ) ) {
                bankList.setBankIcon( domainValue + bankList.getBankIcon() );
            }
        }
        return RspBase.ok( effectList.stream().map( configBankList -> {
            RspConfigBankList rspConfigBankList = new RspConfigBankList();
            BeanUtils.copyProperties( configBankList, rspConfigBankList );
            return rspConfigBankList;
        } ).toList() );
    }
}
