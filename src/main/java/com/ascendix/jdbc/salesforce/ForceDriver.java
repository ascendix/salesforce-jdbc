package com.ascendix.jdbc.salesforce;

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

import org.apache.commons.lang3.StringUtils;

import com.sforce.ws.ConnectionException;

public class ForceDriver implements Driver {

    public static final String ACCEPTABLE_URL = "jdbc:ascendix:salesforce";
    public static final String DEFAULT_API_VERSION = "39.0";
    public static final String DEFAULT_LOGIN_DOMAIN = "login.salesforce.com";

    static {
	try {
	    DriverManager.registerDriver(new ForceDriver());
	} catch (Exception e) {
	    throw new Error(e);
	}
    }

    @Override
    public Connection connect(String url, Properties properties) throws SQLException {
	try {
	    if (!acceptsURL(url)) {
		throw new SQLException("Unknown URL format \"" + url + "\"");
	    }
	    Properties connStringProps = getConnStringProperties(url);
	    properties.putAll(connStringProps);
	    ForceConnectionInfo info = new ForceConnectionInfo();
	    info.setUserName(properties.getProperty("user"));
	    info.setPassword(properties.getProperty("password"));
	    info.setSessionId(properties.getProperty("sessionId"));
	    info.setLoginDomain(properties.getProperty("loginDomain", DEFAULT_LOGIN_DOMAIN));
	    info.setApiVersion(DEFAULT_API_VERSION);
	    info.setServiceEndpoint(getServiceEndpoint(info));
	    return new ForceConnection(info);
	} catch (ConnectionException | IOException e) {
	    throw new SQLException(e);
	}
    }

    private String getServiceEndpoint(ForceConnectionInfo connectionInfo) throws IOException {
	return StringUtils.isEmpty(connectionInfo.getSessionId()) ? null
		: ForceService.getPartnerUrl(connectionInfo.getSessionId(), connectionInfo.getLoginDomain(), connectionInfo.getApiVersion());
    }

    protected Properties getConnStringProperties(String url) throws IOException {
	Properties result = new Properties();
	Matcher matcher = Pattern.compile("\\A" + ACCEPTABLE_URL + "://(.*)").matcher(url);
	if (matcher.matches()) {
	    String urlProperties = matcher.group(1);
	    urlProperties = urlProperties.replaceAll(";", "\n");
	    try (InputStream in = new ByteArrayInputStream(urlProperties.getBytes(StandardCharsets.UTF_8))) {
		result.load(in);
	    }
	}
	return result;
    }

    public boolean acceptsURL(String url) throws SQLException {
	return url != null && url.startsWith(ACCEPTABLE_URL);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
	return new DriverPropertyInfo[] {};
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
	return null;
    }

}
