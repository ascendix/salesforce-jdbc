package com.ascendix.jdbc.salesforce;

public class ForceConnectionInfo {

    private String userName;
    private String password;
    private String clientId;
    private String clientSecret;

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setUserName(String userName) {
	this.userName = userName;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public void setClientId(String clientId) {
	this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
	this.clientSecret = clientSecret;
    }

}
