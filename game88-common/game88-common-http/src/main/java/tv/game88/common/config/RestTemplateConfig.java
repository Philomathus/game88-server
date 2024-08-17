package tv.game88.common.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    @Lazy
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate( clientHttpRequestFactory() );
        //设置字符集
        setCharset( restTemplate );
        return restTemplate;
    }

    @Bean( "restUploadTemplate" )
    @Lazy
    public RestTemplate uploadTemplate() {
        RestTemplate restTemplate = new RestTemplate( clientUploadHttpRequestFactory() );
        //设置字符集
        setCharset( restTemplate );
        return restTemplate;
    }

    @Bean( "restNoRedirectTemplate" )
    @Lazy
    public RestTemplate noRedirectTemplate() {
        RestTemplate restTemplate = new RestTemplate( clientNoRedirectHttpRequestFactory() );
        //设置字符集
        setCharset( restTemplate );
        return restTemplate;
    }

    //设置字符集为UTF-8, 解决乱码问题
    private void setCharset( RestTemplate restTemplate ) {
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for ( HttpMessageConverter<?> messageConverter : messageConverters ) {
            if ( messageConverter instanceof StringHttpMessageConverter ) {
                ( ( StringHttpMessageConverter ) messageConverter ).setDefaultCharset( StandardCharsets.UTF_8 );
            }
        }
    }

    private HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        try {
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient( getHttpClient() );
            requestFactory.setConnectionRequestTimeout( 6000 );
            requestFactory.setConnectTimeout( 6000 );
            return requestFactory;
        } catch ( NoSuchAlgorithmException | KeyStoreException | KeyManagementException e ) {
            throw new RuntimeException( e );
        }
    }

    private HttpComponentsClientHttpRequestFactory clientUploadHttpRequestFactory() {
        try {
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient( getHttpClient() );
            requestFactory.setConnectionRequestTimeout( 60000 );
            requestFactory.setConnectTimeout( 60000 );
            return requestFactory;
        } catch ( NoSuchAlgorithmException | KeyStoreException | KeyManagementException e ) {
            throw new RuntimeException( e );
        }
    }

    private HttpComponentsClientHttpRequestFactory clientNoRedirectHttpRequestFactory() {
        try {
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient( getHttpClientNoRedirect() );
            requestFactory.setConnectionRequestTimeout( 6000 );
            requestFactory.setConnectTimeout( 6000 );
            return requestFactory;
        } catch ( NoSuchAlgorithmException | KeyStoreException | KeyManagementException e ) {
            throw new RuntimeException( e );
        }
    }

    private CloseableHttpClient getHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial( null, ( x509Certificates, s ) -> true ).build();

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory( sslContext );
        return HttpClients
                .custom()
                .setConnectionManager( PoolingHttpClientConnectionManagerBuilder
                        .create()
                        .setMaxConnTotal( 50000 )
                        .setMaxConnPerRoute( 500 )
                        .setSSLSocketFactory( socketFactory )
                        .build() )
                .build();
    }

    private CloseableHttpClient getHttpClientNoRedirect() throws NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial( null, ( x509Certificates, s ) -> true ).build();

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory( sslContext );
        return HttpClients
                .custom()
                .disableRedirectHandling()
                .setConnectionManager( PoolingHttpClientConnectionManagerBuilder
                        .create()
                        .setMaxConnTotal( 50000 )
                        .setMaxConnPerRoute( 500 )
                        .setSSLSocketFactory( socketFactory )
                        .build() )
                .build();
    }
}
 