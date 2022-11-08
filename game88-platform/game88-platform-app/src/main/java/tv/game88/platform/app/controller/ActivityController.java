package tv.game88.platform.app.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;
import tv.game88.platform.api.service.ActivityService;

import javax.annotation.Resource;

@RestController
@Tag( name = "活动及任务相关接口" )
@Log4j2
public class ActivityController {
    @Resource
    private ActivityService activityService;


}
