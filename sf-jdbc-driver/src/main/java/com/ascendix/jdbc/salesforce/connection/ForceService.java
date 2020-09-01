package com.ascendix.jdbc.salesforce.connection;

import com.ascendix.salesforce.oauth.ForceOAuthClient;
import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.concurrent.TimeUnit;

@UtilityClass
@Slf4j
public class ForceService {

    public static final String DEFAULT_LOGIN_DOMAIN = "login.salesforce.com";
    private static final String SANDBOX_LOGIN_DOMAIN = "test.salesforce.com";
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10);
    private static final long READ_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    public static final String DEFAULT_API_VERSION = "43.0";
    public static final int EXPIRE_AFTER_CREATE = 60;
    public static final int EXPIRE_STORE_SIZE = 16;


    private static final DB cacheDb = DBMaker.tempFileDB().closeOnJvmShutdown().make();

    private static HTreeMap<String, String> partnerUrlCache = cacheDb
            .hashMap("PartnerUrlCache", Serializer.STRING, Serializer.STRING)
            .expireAfterCreate(EXPIRE_AFTER_CREATE, TimeUnit.MINUTES)
            .expireStoreSize(EXPIRE_STORE_SIZE * FileUtils.ONE_MB)
            .create();


    private static String getPartnerUrl(String accessToken, boolean sandbox) {
        return partnerUrlCache.computeIfAbsent(accessToken, s -> getPartnerUrlFromUserInfo(accessToken, sandbox));
    }

    private static String getPartnerUrlFromUserInfo(String accessToken, boolean sandbox) {
        return new ForceOAuthClient(CONNECTION_TIMEOUT, READ_TIMEOUT).getUserInfo(accessToken, sandbox).getPartnerUrl();
    }

    public static PartnerConnection createPartnerConnection(ForceConnectionInfo info) throws ConnectionException {
        return info.getSessionId() != null ? createConnectionBySessionId(info) : createConnectionByUserCredential(info);
    }

    private static PartnerConnection createConnectionBySessionId(ForceConnectionInfo info) throws ConnectionException {
        ConnectorConfig partnerConfig = new ConnectorConfig();
        partnerConfig.setSessionId(info.getSessionId());

        if (info.getSandbox() != null) {
            partnerConfig.setServiceEndpoint(ForceService.getPartnerUrl(info.getSessionId(), info.getSandbox()));
            return Connector.newConnection(partnerConfig);
        }

        try {
            partnerConfig.setServiceEndpoint(ForceService.getPartnerUrl(info.getSessionId(), false));
            return Connector.newConnection(partnerConfig);
        } catch (RuntimeException re) {
            try {
                partnerConfig.setServiceEndpoint(ForceService.getPartnerUrl(info.getSessionId(), true));
                return Connector.newConnection(partnerConfig);
            } catch (RuntimeException r) {
                throw new ConnectionException(r.getMessage());
            }
        }
    }

    private static PartnerConnection createConnectionByUserCredential(ForceConnectionInfo info)
            throws ConnectionException {

        ConnectorConfig partnerConfig = new ConnectorConfig();
        partnerConfig.setUsername(info.getUserName());
        partnerConfig.setPassword(info.getPassword());

        PartnerConnection connection;

        if (info.getSandbox() != null) {
            partnerConfig.setAuthEndpoint(buildAuthEndpoint(info));
            connection = Connector.newConnection(partnerConfig);
        } else {

            try {
                info.setSandbox(false);
                partnerConfig.setAuthEndpoint(buildAuthEndpoint(info));
                connection = Connector.newConnection(partnerConfig);
            } catch (ConnectionException ce) {
                info.setSandbox(true);
                partnerConfig.setAuthEndpoint(buildAuthEndpoint(info));
                connection = Connector.newConnection(partnerConfig);
            }
        }
        if (connection != null && StringUtils.isNotBlank(info.getClientName())) {
            connection.setCallOptions(info.getClientName(), null);
        }
        return connection;
    }

    private static String buildAuthEndpoint(ForceConnectionInfo info) {
        String protocol = info.getHttps() ? "https" : "http";
        String domain = info.getSandbox() ? SANDBOX_LOGIN_DOMAIN : info.getLoginDomain() != null ? info.getLoginDomain() : DEFAULT_LOGIN_DOMAIN;
        return String.format("%s://%s/services/Soap/u/%s", protocol, domain, info.getApiVersion());
    }
}
