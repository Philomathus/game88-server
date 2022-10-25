package tv.game88.common.utils;

public class AppVersionUtils {
    public static boolean hasNewVersion( String oldVersion, String curVersion ) {
        if ( oldVersion == null || curVersion == null ) {
            return false;
        }
        String[] old = oldVersion.split( "[.]" );
        String[] cur = curVersion.split( "[.]" );
        if ( old.length != cur.length ) {
            return false;
        }

        for ( int i = 0; i < old.length; i++ ) {
            if ( Integer.parseInt( cur[ i ] ) < Integer.parseInt( old[ i ] ) ) {
                return false;
            }
            if ( Integer.parseInt( cur[ i ] ) > Integer.parseInt( old[ i ] ) ) {
                return true;
            }
        }
        return false;
    }
}
