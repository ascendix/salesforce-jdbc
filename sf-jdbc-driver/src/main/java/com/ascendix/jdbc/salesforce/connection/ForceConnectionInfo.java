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
}
