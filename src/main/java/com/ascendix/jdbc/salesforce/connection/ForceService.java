package com.ascendix.jdbc.salesforce.connection;

import com.ascendix.jdbc.salesforce.oauth.ForceOAuthClient;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;

import java.util.concurrent.TimeUnit;

public class ForceService {

    private static final int TIMEOUT = 300000;
    private static final DB cacheDb = DBMaker.tempFileDB().closeOnJvmShutdown().make();

    private static HTreeMap<String, String> partnerUrlCache = cacheDb
            .hashMap("PartnerUrlCache", Serializer.STRING, Serializer.STRING)
            .expireAfterCreate(60, TimeUnit.MINUTES)
            .expireStoreSize(16 * 1024 * 1024 * 1024)
            .create();


    public static String getPartnerUrl(String accessToken, boolean sandbox, String apiVersion) {
        return partnerUrlCache.computeIfAbsent(accessToken, s -> getPartnerUrlFromUserInfo(accessToken, sandbox))
                .replace("{version}", apiVersion);
    }

    private static String getPartnerUrlFromUserInfo(String accessToken, boolean sandbox) {
        return new ForceOAuthClient(TIMEOUT, TIMEOUT, sandbox)
                .getUserInfo(accessToken)
                .getPartnerEndpoint();
    }
}
