package tv.game88.common.utils;

import lombok.extern.log4j.Log4j2;

import javax.servlet.ServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用http工具封装
 *
 * @author MengJun
 */
@Log4j2
public class HttpHelper {
	private static final String URL_REGEX = "(((https|http)?://)?([a-z0-9]+[.])|(www.))\\w+[.|\\/]([a-z0-9]{0,})?[[.]" +
			"([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";

	/**
	 * 判断字符串是否为URL
	 *
	 * @param urls 用户头像key
	 * @return true:是URL、false:不是URL
	 */
	public static boolean isHttpUrl( String urls ) {
		Pattern pat = Pattern.compile( URL_REGEX );
		Matcher mat = pat.matcher( urls.trim() );
		return mat.matches();
	}

	public static String getBodyString( ServletRequest request ) {
		StringBuilder  sb     = new StringBuilder();
		BufferedReader reader = null;
		try ( InputStream inputStream = request.getInputStream() ) {
			reader = new BufferedReader( new InputStreamReader( inputStream, StandardCharsets.UTF_8 ) );
			String line = "";
			while ( ( line = reader.readLine() ) != null ) {
				sb.append( line );
			}
		} catch ( IOException e ) {
			log.warn( "getBodyString出现问题！" );
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch ( IOException e ) {
					log.error( e.getMessage() );
				}
			}
		}
		return sb.toString();
	}

	public static boolean isConnServerByHttp( String serverUrl ) {
		boolean           connFlag = false;
		URL               url;
		HttpURLConnection conn     = null;
		try {
			url = new URL( serverUrl );
			conn = ( HttpURLConnection ) url.openConnection();
			conn.setConnectTimeout( 5 * 1000 );
			if ( conn.getResponseCode() == 200 ) {
				connFlag = true;
			}
		} catch ( IOException e ) {
			log.error( e.getMessage() );
		} finally {
			if ( conn != null ) {
				conn.disconnect();
			}
		}
		return connFlag;
	}
}
