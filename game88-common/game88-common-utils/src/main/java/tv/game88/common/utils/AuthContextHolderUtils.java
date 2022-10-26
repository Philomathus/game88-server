package tv.game88.common.utils;

import org.springframework.security.core.Authentication;

public class AuthContextHolderUtils {
    private static final ThreadLocal<Authentication> contextHolder = new ThreadLocal<>();

    public static Authentication getContext() {
        return contextHolder.get();
    }

    public static void setContext( Authentication context ) {
        contextHolder.set( context );
    }

    public static void clearContext() {
        contextHolder.remove();
    }
}
