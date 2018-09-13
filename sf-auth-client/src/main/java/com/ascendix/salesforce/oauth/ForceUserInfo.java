package com.ascendix.salesforce.oauth;

import com.google.api.client.util.Key;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ForceUserInfo {

    @Key("user_id")
    private String userId;
    @Key("organization_id")
    private String organizationId;
    @Key("preferred_username")
    private String preferredUsername;
    @Key("nickname")
    private String nickName;
    private String name;
    private String email;
    @Key("zoneinfo")
    private String timeZone;
    @Key("locale")
    private String locale;
    private String instance;
    private String partnerUrl;
    @Key("urls")
    private Map<String, String> urls;
}
