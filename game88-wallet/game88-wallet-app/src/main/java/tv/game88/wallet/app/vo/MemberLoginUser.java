package tv.game88.wallet.app.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tv.game88.common.utils.JsonUtil;
import tv.game88.common.utils.StringUtils;
import tv.game88.wallet.api.vo.PlatformUser;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
public class MemberLoginUser implements UserDetails {
    /**
     * 登录时间
     */
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss" )
    private LocalDateTime loginTime;
    /**
     * 登录IP地址
     */
    private String       loginIp;
    /**
     * 用户信息
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private PlatformUser platformUser;

    private String platformUserStr;

    private String userId;

    public MemberLoginUser() {
    }

    public MemberLoginUser( PlatformUser platformUser ) {
        this.setPlatformUser( platformUser );
    }

    public void setPlatformUser( PlatformUser platformUser ) {
        if ( platformUser != null ) {
            this.platformUserStr = JsonUtil.object2Json( platformUser );
        }
        this.platformUser = platformUser;
    }

    public void setPlatformUserStr( String platformUserStr ) {
        if ( StringUtils.isNotBlank( platformUserStr ) ) {
            this.platformUser = JsonUtil.json2Object( platformUserStr, PlatformUser.class );
        }
        this.platformUserStr = platformUserStr;
    }

    public String getUserId() {
        return platformUser.getId();
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public String getPassword() {
        return platformUser.getPassword();
    }

    @Override
    public String getUsername() {
        return platformUser.getId();
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Override
    public boolean isEnabled() {
        return true;
    }
}
