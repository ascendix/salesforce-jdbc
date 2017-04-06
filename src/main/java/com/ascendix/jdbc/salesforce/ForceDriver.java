package com.ascendix.jdbc.salesforce;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import com.sforce.ws.ConnectionException;

public class ForceDriver implements Driver {

    private static final String URL_PROPERTY_CLIENT_SECRET = "clientSecret";
    private static final String URL_PROPERTY_CLIENT_ID = "clientId";
    public static final String ACCEPTABLE_URL = "jdbc:ascendix:salesforce";
    private ForceConnectionInfo info;

    static {
	try {
	    DriverManager.registerDriver(new ForceDriver());
	} catch (Exception e) {
	    throw new Error(e);
	}
    }

    public Connection connect(String url, Properties info) throws SQLException {
	try {
	    if (!acceptsURL(url)) {
		throw new SQLException("Unknown URL format \"" + url + "\"");
	    }
	    this.info = new ForceConnectionInfo();
	    this.info.setUserName(info.getProperty("user"));
	    this.info.setPassword(info.getProperty("password"));
	    this.info.setClientId(getUrlProperty(url, URL_PROPERTY_CLIENT_ID));
	    this.info.setClientSecret(getUrlProperty(url, URL_PROPERTY_CLIENT_SECRET));
	    return new ForceConnection(this.info);
	} catch (ConnectionException e) {
	    throw new SQLException(e);
	}
    }

    protected String getUrlProperty(String url, String propertyName) {
	return Arrays.stream(url.split("//|:|;"))
		.map(p -> p.replaceFirst("\\s*", ""))
		.filter(p -> p.replaceFirst("\\s*=.*", "").equals(propertyName))
		.map(p -> p.replaceFirst(".*=", ""))
		.findFirst()
		.orElse(null);
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
	return 0;
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
