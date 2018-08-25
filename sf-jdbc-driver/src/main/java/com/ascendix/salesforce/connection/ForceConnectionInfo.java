package com.ascendix.salesforce.connection;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ForceConnectionInfo {

    private String userName;
    private String password;
    private String sessionId;
    private String apiVersion;
    private Boolean sandbox;
}
