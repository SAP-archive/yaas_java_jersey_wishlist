package com.sap.wishlist.api.generated;

import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.server.ContainerRequest;
import org.springframework.stereotype.Component;

import com.sap.wishlist.service.WishlistMediaService;
import com.sap.wishlist.service.WishlistService;

/**
 * Resource class containing the custom logic.
 */
@Component("apiWishlistsResource")
@Singleton
public class DefaultWishlistsResource implements WishlistsResource {
	@javax.ws.rs.core.Context
	private javax.ws.rs.core.UriInfo uriInfo;

	@Context
	private ContainerRequest request;

	@Inject
	private WishlistService wishlistService;
	@Inject
	private WishlistMediaService wishlistMediaService;

	/* GET / */
	@Override
	public Response get(final PagedParameters paged,
			final YaasAwareParameters yaasAware) {
		return wishlistService.get(paged, yaasAware);
	}

	/* POST / */
	@Override
	public Response post(YaasAwareParameters yaasAware, Wishlist wishlist) {
		return wishlistService.post(yaasAware, uriInfo, wishlist);
	}

	/* GET //{wishlistId} */
	@Override
	public Response getByWishlistId(YaasAwareParameters yaasAware,
			String wishlistId) {
		return wishlistService.getByWishlistId(yaasAware, wishlistId);
	}

	/* PUT //{wishlistId} */
	@Override
	public Response putByWishlistId(YaasAwareParameters yaasAware,
			String wishlistId, Wishlist wishlist) {
		return wishlistService.putByWishlistId(yaasAware, wishlistId, wishlist);
	}

	/* DELETE //{wishlistId} */
	@Override
	public Response deleteByWishlistId(YaasAwareParameters yaasAware,
			String wishlistId) {
		return wishlistService.deleteByWishlistId(yaasAware, wishlistId);
	}

	/* POST //{wishlistId}/media */
	@Override
	public Response postByWishlistIdMedia(YaasAwareParameters yaasAware,
			String wishlistId) {
		final FormDataMultiPart multiPart = request
				.readEntity(FormDataMultiPart.class);
		final InputStream fileInputStream = ((BodyPartEntity) multiPart
				.getField("file").getEntity()).getInputStream();

		return wishlistMediaService.postByWishlistIdMedia(yaasAware,
				wishlistId, fileInputStream, uriInfo.getRequestUri());
	}

	/* GET //{wishlistId}/media */
	@Override
	public Response getByWishlistIdMedia(PagedParameters paged,
			YaasAwareParameters yaasAware, String wishlistId) {
		return wishlistMediaService.getByWishlistIdMedia(paged, yaasAware,
				wishlistId);
	}

	/* DELETE //{wishlistId}/media/{mediaId} */
	@Override
	public Response deleteByWishlistIdMediaByMediaId(
			YaasAwareParameters yaasAware, String wishlistId, String mediaId) {
		return wishlistMediaService.deleteByWishlistIdMediaByMediaId(yaasAware,
				wishlistId, mediaId);
	}
}
