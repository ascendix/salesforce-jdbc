package com.ascendix.jdbc.salesforce.connection;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ForceConnectionInfo {

    private String userName;
    private String password;
    private String sessionId;
    private Boolean sandbox;
    private Boolean https = true;
    private String apiVersion = ForceService.DEFAULT_API_VERSION;
    private String loginDomain;
}
