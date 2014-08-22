package com.localz.spotz.api.v1;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpResponse;
import com.localz.spotz.api.ApiMethod;
import com.localz.spotz.api.exceptions.LocalzApiException;
import com.localz.spotz.api.models.Response;
import com.localz.spotz.api.models.response.v1.BeaconsGetResponse;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class BeaconsGetApi extends ApiMethod<Void, BeaconsGetResponse[]> {

    private static final String PATH = "/beacons";

    @Override
    public Response<BeaconsGetResponse[]> execute() throws LocalzApiException {
        try {

            HttpResponse httpResponse = httpRequestFactory.buildGetRequest(
                    new GenericUrl(hostUrl + PATH))
                    .setHeaders(createDeviceSignedHeaders(new Date(), HttpMethods.GET, PATH))
                    .execute();

            return response(httpResponse, BeaconsGetResponse.TYPE);

        } catch (IOException e) {
            throw new LocalzApiException("Exception while executing API request: " + BeaconsGetApi.class.getSimpleName(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new LocalzApiException("Exception while creating signature: ", e);
        } catch (InvalidKeyException e) {
            throw new LocalzApiException("Exception while creating signature: ", e);
        }
    }
}
