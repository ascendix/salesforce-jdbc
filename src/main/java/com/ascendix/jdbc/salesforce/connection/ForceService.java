package com.ascendix.jdbc.salesforce.connection;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class ForceService {

    private final static int TIMEOUT = 300000;
    private static DB cacheDb = DBMaker.tempFileDB().closeOnJvmShutdown().make();

    private static HTreeMap<String, String> partnerUrlCache = cacheDb
            .hashMap("PartnerUrlCache", Serializer.STRING, Serializer.STRING)
            .expireAfterCreate(60, TimeUnit.MINUTES)
            .expireStoreSize(16 * 1024 * 1024 * 1024)
            .create();


    public static String getPartnerUrl(String accessToken, String loginDomain, String apiVersion) {
        return partnerUrlCache.computeIfAbsent(accessToken, s -> {
            Object userInfo = null;
            try {
                userInfo = getUserInfo(accessToken, loginDomain);
                return JsonPath.read(userInfo, "$.urls.partner");
            } catch (PathNotFoundException e) {
                throw new RuntimeException(userInfo == null ? "" : userInfo.toString(), e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).replace("{version}", apiVersion);
    }

    private static Object getUserInfo(String accessToken, String loginDomain) throws IOException {
        String userInfoEndpoint = String.format("https://%s/services/oauth2/userinfo", loginDomain);
        HttpGet request = new HttpGet(userInfoEndpoint);
        request.addHeader("Authorization", "Bearer " + accessToken);
        try (CloseableHttpClient client = createHttpClient();
             CloseableHttpResponse response = client.execute(request);
             InputStream in = new BufferedInputStream(response.getEntity().getContent())) {
            return transformToResult(in);
        } finally {
            request.releaseConnection();
        }
    }

    public static Object transformToResult(InputStream in) {
        return Configuration.defaultConfiguration().jsonProvider().parse(in, "UTF-8");
    }

    private static CloseableHttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT)
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .build();

        return HttpClients.custom()
                .setSSLHostnameVerifier(getHostnameVerifier())
                .setSSLContext(getSSLContext())
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    private static HostnameVerifier getHostnameVerifier() {
        return (hostname, session) -> true;
    }

    private static SSLContext getSSLContext() {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
            return ctx;
        } catch (Exception e) {
            return null;
        }
    }

    private static class DefaultTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
