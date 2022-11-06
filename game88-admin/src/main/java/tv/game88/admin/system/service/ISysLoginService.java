package tv.game88.admin.system.service;

import tv.game88.common.vo.RspBase;
import tv.game88.core.admin.vo.LoginBody;

public interface ISysLoginService {
    public RspBase<String> login( LoginBody loginBody ) throws Exception;
}
