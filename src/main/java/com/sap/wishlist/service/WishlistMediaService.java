package com.sap.wishlist.service;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.springframework.beans.factory.annotation.Value;

import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.authorization.DiagnosticContext;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionCallback;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;
import com.sap.cloud.yaas.servicesdk.patternsupport.schemas.ResourceLocation;
import com.sap.wishlist.api.generated.DocumentWishlistMedia;
import com.sap.wishlist.api.generated.DocumentWishlistMediaRead;
import com.sap.wishlist.api.generated.PagedParameters;
import com.sap.wishlist.api.generated.WishlistMedia;
import com.sap.wishlist.api.generated.YaasAwareParameters;
import com.sap.wishlist.client.documentrepository.DocumentClient;
import com.sap.wishlist.client.mediaRepository.MediaClient;
import com.sap.wishlist.utility.AuthorizationHelper;
import com.sap.wishlist.utility.ErrorHandler;

@ManagedBean
public class WishlistMediaService {
	private final String TYPE = "projectMedia";

	@Inject
	private MediaClient mediaClient;
	@Inject
	private DocumentClient documentClient;
	@Inject
	private AuthorizedExecutionTemplate authorizedExecutionTemplate;
	@Inject
	private AuthorizationHelper authorizationHelper;
	@Value("${YAAS_CLIENT}")
	private String client;

	/* POST //{wishlistId}/media */
	public Response postByWishlistIdMedia(YaasAwareParameters yaasAware,
			String wishlistId, final InputStream fileInputStream,
			final URI requestUri) {

		// Prepare MultiPart
		final FormDataMultiPart multiPart = new FormDataMultiPart();
		multiPart.bodyPart(new StreamDataBodyPart("file", fileInputStream));

		Response response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return mediaClient
								.tenant(yaasAware.getHybrisTenant())
								.client(client)
								.media()
								.preparePost()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.withPayload(multiPart,
										MediaType.MULTIPART_FORM_DATA)
								.execute();
					}
				});

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			ErrorHandler.handleResponse(response);
		}

		ResourceLocation location = response.readEntity(ResourceLocation.class);

		// Create document repository entry for media
		final DocumentWishlistMedia documentWishlistMedia = new DocumentWishlistMedia();
		WishlistMedia wishlistMedia = new WishlistMedia();
		wishlistMedia.setId(location.getId());

		documentWishlistMedia.setWishlistId(wishlistId);
		documentWishlistMedia.setWishlistMedia(wishlistMedia);

		response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return documentClient
								.tenant(yaasAware.getHybrisTenant())
								.clientData(client)
								.type(TYPE)
								.preparePost()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.withPayload(documentWishlistMedia).execute();
					}
				});

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			ErrorHandler.handleResponse(response);
		}

		location = response.readEntity(ResourceLocation.class);

		URI resourceURI = URI.create(requestUri.toString() + "/"
				+ location.getId());
		return Response.created(resourceURI).build();
	}

	/* GET //{wishlistId}/media */
	public Response getByWishlistIdMedia(PagedParameters paged,
			YaasAwareParameters yaasAware, String wishlistId) {
		// Return parameter
		ArrayList<WishlistMedia> result = null;

		Response response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return documentClient
								.tenant(yaasAware.getHybrisTenant())
								.clientData(client)
								.type(TYPE)
								.prepareGet()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.withPageNumber(paged.getPageNumber())
								.withPageSize(paged.getPageSize()).execute();
					}
				});

		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			ErrorHandler.handleResponse(response);
		}

		result = new ArrayList<WishlistMedia>();
		for (DocumentWishlistMediaRead documentWishlistMedia : response
				.readEntity(DocumentWishlistMediaRead[].class)) {
			WishlistMedia wishlistMedia = new WishlistMedia();
			wishlistMedia.setId(documentWishlistMedia.getId());
			wishlistMedia.setUri(MediaClient.DEFAULT_BASE_URI + "/"
					+ yaasAware.getHybrisTenant() + "/" + client + "/media/"
					+ documentWishlistMedia.getWishlistMedia().getId());
			result.add(wishlistMedia);
		}

		return Response.ok().entity(result).build();
	}

	/* DELETE //{wishlistId}/media/{mediaId} */
	public Response deleteByWishlistIdMediaByMediaId(
			YaasAwareParameters yaasAware, String wishlistId, String mediaId) {
		Response response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return documentClient
								.tenant(yaasAware.getHybrisTenant())
								.clientData(client)
								.type(TYPE)
								.dataId(mediaId)
								.prepareGet()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.execute();
					}
				});

		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
			ErrorHandler.handleResponse(response);
		}

		DocumentWishlistMediaRead documentWishlistMediaRead = response
				.readEntity(DocumentWishlistMediaRead.class);
		final String mediaRepoId = (String) documentWishlistMediaRead
				.getWishlistMedia().getId();

		response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return documentClient
								.tenant(yaasAware.getHybrisTenant())
								.clientData(client)
								.type(TYPE)
								.dataId(mediaId)
								.prepareDelete()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.execute();
					}
				});

		if (response.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
			ErrorHandler.handleResponse(response);
		}
		response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return mediaClient
								.tenant(yaasAware.getHybrisTenant())
								.client(client)
								.media()
								.mediaId(mediaRepoId)
								.prepareDelete()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.execute();
					}
				});

		return Response.noContent().build();
	}
}