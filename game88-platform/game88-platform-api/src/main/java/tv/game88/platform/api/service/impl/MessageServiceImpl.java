package tv.game88.platform.api.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import tv.game88.platform.api.dto.RspMessageCommonProblem;
import tv.game88.platform.api.dto.RspMessageHomeNotice;
import tv.game88.platform.api.dto.RspMessageOnSite;
import tv.game88.platform.api.service.MessageService;

import java.util.List;

@Log4j2
@Service
public class MessageServiceImpl implements MessageService {

    @Override
    public List<RspMessageHomeNotice> getMessageHomeNotices() {
        return null;
    }

    @Override
    public List<RspMessageCommonProblem> getMessageCommonProblems() {
        return null;
    }

    @Override
    public List<RspMessageOnSite> getMessageOnSites( String userId ) {
        return null;
    }
}
