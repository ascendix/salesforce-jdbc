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
    private static final Pattern URL_HAS_AUTHORIZATION_SEGMENT = Pattern.compile("\\A" + ACCEPTABLE_URL + "://([^:]+):([^@]+)@([^?]*)([?](.*))?");
    private static final Pattern PARAM_STANDARD_PATTERN = Pattern.compile("(([^=]+)=([^&]*)&?)");

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
            /*
             * According to JDBC spec:
             * > The driver should return "null" if it realizes it is the wrong kind of driver to connect to the given URL.
             * > This will be common, as when the JDBC driver manager is asked to connect to a given URL it passes the URL to each loaded driver in turn.
             *
             * Source: https://docs.oracle.com/javase/8/docs/api/java/sql/Driver.html#connect-java.lang.String-java.util.Properties-
             */
            return null;
        }
        try {
            Properties connStringProps = getConnStringProperties(url);
            properties.putAll(connStringProps);
            ForceConnectionInfo info = new ForceConnectionInfo();
            info.setUserName(properties.getProperty("user"));
            info.setClientName(properties.getProperty("client"));
            info.setPassword(properties.getProperty("password"));
            info.setSessionId(properties.getProperty("sessionId"));
            info.setSandbox(resolveSandboxProperty(properties));
            info.setHttps(resolveBooleanProperty(properties, "https", true));
            info.setApiVersion(resolveStringProperty(properties, "api", ForceService.DEFAULT_API_VERSION));
            info.setLoginDomain(resolveStringProperty(properties, "loginDomain", ForceService.DEFAULT_LOGIN_DOMAIN));

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

    private static Boolean resolveBooleanProperty(Properties properties, String propertyName, boolean defaultValue) {
        String boolValue = properties.getProperty(propertyName);
        if (boolValue != null) {
            return Boolean.valueOf(boolValue);
        }
        return defaultValue;
    }

    private static String resolveStringProperty(Properties properties, String propertyName, String defaultValue) {
        String boolValue = properties.getProperty(propertyName);
        if (boolValue != null) {
            return boolValue;
        }
        return defaultValue;
    }


    protected Properties getConnStringProperties(String urlString) throws IOException {
        Properties result = new Properties();
        String urlProperties = null;

        Matcher stdMatcher = URL_PATTERN.matcher(urlString);
        Matcher authMatcher = URL_HAS_AUTHORIZATION_SEGMENT.matcher(urlString);

        if (authMatcher.matches()) {
            result.put("user", authMatcher.group(1));
            result.put("password", authMatcher.group(2));
            result.put("loginDomain", authMatcher.group(3));
            if (authMatcher.groupCount() > 4 && authMatcher.group(5) != null) {
                // has some other parameters - parse them from standard URL format like
                // ?param1=value1&param2=value2
                String parameters = authMatcher.group(5);
                Matcher matcher = PARAM_STANDARD_PATTERN.matcher(parameters);
                while(matcher.find()) {
                    String param = matcher.group(2);
                    String value = 3 >= matcher.groupCount() ? matcher.group(3) : null;
                    result.put(param, value);
                }

            }
        } else if (stdMatcher.matches()) {
            urlProperties = stdMatcher.group(1);
            urlProperties = urlProperties.replaceAll(";", "\n");
        }

        if (urlProperties != null) {
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
