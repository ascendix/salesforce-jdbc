package com.ascendix.jdbc.salesforce;

public class ForceConnectionInfo {

    private String userName;
    private String password;
    private String sessionId;
    private String serviceEndpoint;
    private String apiVersion;
    private String loginDomain;

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setUserName(String userName) {
	this.userName = userName;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public String getSessionId() {
	return sessionId;
    }

    public void setSessionId(String sessionId) {
	this.sessionId = sessionId;
    }

    public String getServiceEndpoint() {
	return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
	this.serviceEndpoint = serviceEndpoint;
    }

    public String getAuthEndpoint() {
	return String.format("https://%s/services/Soap/u/%s", getLoginDomain(), getApiVersion());
    }


    public String getApiVersion() {
	return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
	this.apiVersion = apiVersion;
    }

    public String getLoginDomain() {
	return loginDomain;
    }

    public void setLoginDomain(String loginDomain) {
	this.loginDomain = loginDomain;
    }

}
