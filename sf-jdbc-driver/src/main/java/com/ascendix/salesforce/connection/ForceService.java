package com.ascendix.salesforce.connection;

import com.ascendix.salesforce.oauth.ForceOAuthClient;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ForceService {

    private static final String DEFAULT_LOGIN_DOMAIN = "login.salesforce.com";
    private static final String SANDBOX_LOGIN_DOMAIN = "test.salesforce.com";
    private static final int TIMEOUT = 300000;

    private static final DB cacheDb = DBMaker.tempFileDB().closeOnJvmShutdown().make();

    private static HTreeMap<String, String> partnerUrlCache = cacheDb
            .hashMap("PartnerUrlCache", Serializer.STRING, Serializer.STRING)
            .expireAfterCreate(60, TimeUnit.MINUTES)
            .expireStoreSize(16 * 1024 * 1024 * 1024)
            .create();


    private static String getPartnerUrl(String accessToken, boolean sandbox, String apiVersion) {
        return partnerUrlCache.computeIfAbsent(accessToken, s -> getPartnerUrlFromUserInfo(accessToken, sandbox))
                .replace("{version}", apiVersion);
    }

    private static String getPartnerUrlFromUserInfo(String accessToken, boolean sandbox) {
        Map<String, String> urls = new ForceOAuthClient(TIMEOUT, TIMEOUT, sandbox)
                .getUserInfo(accessToken).getUrls();
        if (urls == null || !urls.containsKey("partner")) {
            throw new IllegalStateException("User info doesn't contain partner URL: " + urls);
        }
        return urls.get("partner");
    }

    public static PartnerConnection createPartnerConnection(ForceConnectionInfo info) throws ConnectionException {
        return info.getSessionId() != null ? createConnectionBySessionId(info) : createConnectionByUserCredential(info);
    }

    private static PartnerConnection createConnectionBySessionId(ForceConnectionInfo info) throws ConnectionException {
        ConnectorConfig partnerConfig = new ConnectorConfig();
        partnerConfig.setSessionId(info.getSessionId());

        if (info.getSandbox() != null) {
            partnerConfig.setServiceEndpoint(ForceService.getPartnerUrl(info.getSessionId(), info.getSandbox(), info.getApiVersion()));
            return Connector.newConnection(partnerConfig);
        }

        try {
            partnerConfig.setServiceEndpoint(ForceService.getPartnerUrl(info.getSessionId(), false, info.getApiVersion()));
            return Connector.newConnection(partnerConfig);
        } catch (RuntimeException re) {
            try {
                partnerConfig.setServiceEndpoint(ForceService.getPartnerUrl(info.getSessionId(), true, info.getApiVersion()));
                return Connector.newConnection(partnerConfig);
            } catch (RuntimeException r) {
                throw new ConnectionException(r.getMessage());
            }
        }
    }

    private static PartnerConnection createConnectionByUserCredential(ForceConnectionInfo info) throws ConnectionException {
        ConnectorConfig partnerConfig = new ConnectorConfig();
        partnerConfig.setUsername(info.getUserName());
        partnerConfig.setPassword(info.getPassword());
        if (info.getSandbox() != null) {
            partnerConfig.setAuthEndpoint(buildAuthEndpoint(info.getSandbox(), info.getApiVersion()));
            return Connector.newConnection(partnerConfig);
        }

        try {
            partnerConfig.setAuthEndpoint(buildAuthEndpoint(false, info.getApiVersion()));
            return Connector.newConnection(partnerConfig);
        } catch (ConnectionException ce) {
            partnerConfig.setAuthEndpoint(buildAuthEndpoint(true, info.getApiVersion()));
            return Connector.newConnection(partnerConfig);
        }
    }

    private static String buildAuthEndpoint(boolean sandbox, String apiVersion) {
        return String.format("https://%s/services/Soap/u/%s", sandbox ? SANDBOX_LOGIN_DOMAIN : DEFAULT_LOGIN_DOMAIN, apiVersion);
    }

}
