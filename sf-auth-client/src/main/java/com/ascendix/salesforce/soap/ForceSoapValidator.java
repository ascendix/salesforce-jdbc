package com.ascendix.salesforce.soap;

import com.ascendix.salesforce.oauth.ForceClientException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.ExponentialBackOff;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class ForceSoapValidator {
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private final int connectTimeout;
    private final int readTimeout;

    public ForceSoapValidator(int connectTimeoutSec, int readTimeoutSec) {
        this.connectTimeout = connectTimeoutSec * 1000;
        this.readTimeout = readTimeoutSec * 1000;
    }

    public boolean validateForceToken(String partnerUrl, String accessToken) {
        HttpRequestFactory requestFactory = buildHttpRequestFactory();
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String requestBody = IOUtils.toString(classLoader.getResourceAsStream("forceSoapBody"));

            HttpRequest request = requestFactory.buildPostRequest(
                    new GenericUrl(partnerUrl),
                    ByteArrayContent.fromString(
                            "text/xml",
                            requestBody.replace("{sessionId}", accessToken)
                    ));
            HttpHeaders headers = request.getHeaders();
            headers.set("SOAPAction", "some");
            HttpResponse result = request.execute();
            return result.getStatusCode() == HttpStatusCodes.STATUS_CODE_OK;
        } catch (HttpResponseException e) {
            throw new ForceClientException("Response error: " + e.getStatusCode() + " " + e.getContent());
        } catch (IOException e) {
            throw new ForceClientException("IO error: " + e.getMessage(), e);
        }
    }

    private HttpRequestFactory buildHttpRequestFactory() {
        return HTTP_TRANSPORT.createRequestFactory(
                request -> {
                    request.setConnectTimeout(connectTimeout);
                    request.setReadTimeout(readTimeout);
                    request.setUnsuccessfulResponseHandler(buildUnsuccessfulResponseHandler());
                });
    }

    private static HttpBackOffUnsuccessfulResponseHandler buildUnsuccessfulResponseHandler() {

        ExponentialBackOff backOff = new ExponentialBackOff.Builder()
                .setInitialIntervalMillis(500)
                .setMaxElapsedTimeMillis(30000)
                .setMaxIntervalMillis(10000)
                .setMultiplier(1.5)
                .setRandomizationFactor(0.5)
                .build();
        return new HttpBackOffUnsuccessfulResponseHandler(backOff);
    }
}
