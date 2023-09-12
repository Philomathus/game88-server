package tv.game88.core.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tv.game88.core.config.cache.ConfigOssCacheUtil;
import tv.game88.core.config.mapper.ConfigOssMapper;

import javax.annotation.Resource;

@Log4j2
@Component
public class OssApi {
    @Resource
    private ConfigOssMapper    configOssMapper;
    @Resource
    private ConfigOssCacheUtil configOssCacheUtil;


}
