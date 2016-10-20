
package com.sap.wishlist.media;

import java.io.InputStream;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.wishlist.api.generated.YaasAwareParameters;
import com.sap.wishlist.client.media.SecureMediaClient;
import com.sap.wishlist.utility.ErrorHandler;

@ManagedBean
public class MediaClientService {

    @Inject
    private SecureMediaClient mediaClient;

    @Value("${YAAS_CLIENT}")
    private String client;

    public JSONObject createMedia(final YaasAwareParameters yaasAware, final InputStream fileInputStream,
            final AccessToken token) {

        final FormDataMultiPart multiPart = new FormDataMultiPart();
        multiPart.bodyPart(new StreamDataBodyPart("file", fileInputStream));

        final JSONObject payload = new JSONObject();
        payload.put("Content-Type", MediaType.MULTIPART_FORM_DATA);

        final Response response = mediaClient // get URL
                .$public()
                .files()
                .preparePost()
                .withAuthorization(token.toAuthorizationHeaderValue())
                .withPayload(payload.toString())
                .execute();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            final JSONObject mediaCreate = new JSONObject(response.readEntity(String.class));
            final String uploadLink = mediaCreate.getString("uploadLink");
            final String uploadEndpointId = mediaCreate.getString("id");
            final WebTarget webTarget = ClientBuilder.newClient().target(uploadLink);
            final Response response1 = webTarget.request() // put media
                    .put(Entity.entity(fileInputStream, MediaType.MULTIPART_FORM_DATA));
            if (response1.getStatus() == Response.Status.OK.getStatusCode()) {
                final Response response2 = mediaClient.$public().files().fileId(uploadEndpointId).commit() // commit
                                                                                                           // media
                        .preparePost().withAuthorization(token.toAuthorizationHeaderValue()).execute();
                if (response2.getStatus() == Response.Status.OK.getStatusCode()) {
                    final JSONObject mediaCommited = new JSONObject(response2.readEntity(String.class));
                    return mediaCommited;
                }
            }
        }

        throw ErrorHandler.resolveErrorResponse(response, token);
    }

    public void deleteMedia(final YaasAwareParameters yaasAware, final String id, final AccessToken token) {
        final Response response = mediaClient
                .$public()
                .files()
                .fileId(id)
                .prepareDelete()
                .withAuthorization(token.toAuthorizationHeaderValue())
                .execute();

        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            return;
        }

        throw ErrorHandler.resolveErrorResponse(response, token);
    }
}
