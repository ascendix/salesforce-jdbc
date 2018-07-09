package com.ascendix.jdbc.salesforce;

import com.ascendix.jdbc.salesforce.connection.ForceConnection;
import com.ascendix.jdbc.salesforce.connection.ForceConnectionInfo;
import com.ascendix.jdbc.salesforce.connection.ForceService;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class ForceDriver implements Driver {

    private static final String ACCEPTABLE_URL = "jdbc:ascendix:salesforce";
    private static final Pattern URL_PATTERN = Pattern.compile("\\A" + ACCEPTABLE_URL + "://(.*)");
    private static final String DEFAULT_API_VERSION = "39.0";

    static {
        try {
            DriverManager.registerDriver(new ForceDriver());
        } catch (Exception e) {
            throw new RuntimeException("Failed register ForceDriver: " + e.getMessage(), e);
        }
    }

    @Override
    public Connection connect(String url, Properties properties) throws SQLException {
        if (!acceptsURL(url)) {
            throw new SQLException("Unknown URL format \"" + url + "\"");
        }
        try {
            Properties connStringProps = getConnStringProperties(url);
            properties.putAll(connStringProps);
            ForceConnectionInfo info = new ForceConnectionInfo();
            info.setUserName(properties.getProperty("user"));
            info.setPassword(properties.getProperty("password"));
            info.setSessionId(properties.getProperty("sessionId"));
            info.setSandbox(resolveSandboxProperty(properties));
            info.setApiVersion(DEFAULT_API_VERSION);

            PartnerConnection partnerConnection = ForceService.createPartnerConnection(info);
            return new ForceConnection(partnerConnection);
        } catch (ConnectionException | IOException e) {
            throw new SQLException(e);
        }
    }

    private static Boolean resolveSandboxProperty(Properties properties) {
        String sandbox = properties.getProperty("sandbox");
        if (sandbox != null) {
            return Boolean.valueOf(sandbox);
        }
        String loginDomain = properties.getProperty("loginDomain");
        if (loginDomain != null) {
            return loginDomain.contains("test");
        }
        return null;
    }


    protected Properties getConnStringProperties(String url) throws IOException {
        Properties result = new Properties();
        Matcher matcher = URL_PATTERN.matcher(url);
        if (matcher.matches()) {
            String urlProperties = matcher.group(1);
            urlProperties = urlProperties.replaceAll(";", "\n");
            try (InputStream in = new ByteArrayInputStream(urlProperties.getBytes(StandardCharsets.UTF_8))) {
                result.load(in);
            }
        }
        return result;
    }

    @Override
    public boolean acceptsURL(String url) {
        return url != null && url.startsWith(ACCEPTABLE_URL);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
        return new DriverPropertyInfo[]{};
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 1;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

}
