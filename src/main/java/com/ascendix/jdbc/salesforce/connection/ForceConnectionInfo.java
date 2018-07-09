package com.ascendix.jdbc.salesforce.connection;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ForceConnectionInfo {

    private String userName;
    private String password;
    private String sessionId;
    private String apiVersion;
    private String loginDomain;
    private Boolean sandbox;

    public String getAuthEndpoint() {
        return String.format("https://%s/services/Soap/u/%s", getLoginDomain(), getApiVersion());
    }
}
