package tv.game88.platform.api.service.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tv.game88.core.config.cache.ConfigDomainCacheUtil;
import tv.game88.platform.api.cache.MessageCacheUtil;
import tv.game88.platform.api.dto.RspMessageCommonProblem;
import tv.game88.platform.api.dto.RspMessageHomeNotice;
import tv.game88.platform.api.dto.RspMessageOnSite;
import tv.game88.platform.api.service.MessageService;

import javax.annotation.Resource;
import java.util.List;

@Log4j2
@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private MessageCacheUtil messageCacheUtil;

    @Override
    public List<RspMessageHomeNotice> getMessageHomeNotices() {
        return messageCacheUtil.getMessageHomeNotices();
    }

    @Override
    public List<RspMessageCommonProblem> getMessageCommonProblems() {
        List<RspMessageCommonProblem> messageCommonProblems = messageCacheUtil.getMessageCommonProblems();
        String                        domainValue           = ConfigDomainCacheUtil.me.getDomainOssValue();
        for ( RspMessageCommonProblem commonProblem : messageCommonProblems ) {
            // 替换动态域名
            if ( StringUtils.isNotBlank( commonProblem.getContent() ) ) {
                commonProblem.setContent( commonProblem.getContent().replaceAll( "\\$\\{domain\\.oss\\}", domainValue ) );
            }
        }
        return messageCommonProblems;
    }

    @Override
    public List<RspMessageOnSite> getMessageOnSites( String userId ) {
        return messageCacheUtil.getMessageOnSites();
    }
}
