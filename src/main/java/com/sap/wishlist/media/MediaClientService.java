
package com.sap.wishlist.media;

import java.io.InputStream;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.springframework.beans.factory.annotation.Value;

import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.patternsupport.schemas.ResourceLocation;
import com.sap.wishlist.api.generated.YaasAwareParameters;
import com.sap.wishlist.client.media.MediaClient;
import com.sap.wishlist.utility.ErrorHandler;


@ManagedBean
public class MediaClientService {

	@Inject
	private MediaClient mediaClient;

	@Value("${YAAS_CLIENT}")
	private String client;

	public ResourceLocation createMedia(final YaasAwareParameters yaasAware, final InputStream fileInputStream,
			final AccessToken token) {

		final FormDataMultiPart multiPart = new FormDataMultiPart();
		multiPart.bodyPart(new StreamDataBodyPart("file", fileInputStream));

		final Response response = mediaClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.media()
				.preparePost()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.withPayload(multiPart, MediaType.MULTIPART_FORM_DATA)
				.execute();

		if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
			return response.readEntity(ResourceLocation.class);
		}

		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public void deleteMedia(final YaasAwareParameters yaasAware, final String id, final AccessToken token) {
		final Response response = mediaClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.media()
				.mediaId(id)
				.prepareDelete()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.execute();

		if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			return;
		}

		throw ErrorHandler.resolveErrorResponse(response, token);
	}
}
