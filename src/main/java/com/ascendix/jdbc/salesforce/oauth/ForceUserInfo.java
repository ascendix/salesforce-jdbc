package com.ascendix.jdbc.salesforce.oauth;

import com.google.api.client.util.Key;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ForceUserInfo {

    @Key("user_id")
    private String userId;
    @Key("organization_id")
    private String organizationId;
    @Key("preferred_username")
    private String preferredUsername;
    @Key("profile")
    private String profileUrl;
    @Key("zoneinfo")
    private String timeZone;
    @Key("locale")
    private String locale;

    @Key("partner")
    private String partnerEndpoint;

    @Key("metadata")
    private String metadataEndpoint;
}
