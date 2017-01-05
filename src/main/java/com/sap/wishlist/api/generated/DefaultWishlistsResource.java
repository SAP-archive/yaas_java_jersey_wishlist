package com.sap.wishlist.api.generated;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.BodyPartEntity;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.server.ContainerRequest;
import org.springframework.stereotype.Component;

import com.sap.cloud.yaas.servicesdk.jerseysupport.pagination.PaginatedCollection;
import com.sap.cloud.yaas.servicesdk.jerseysupport.pagination.PaginationRequest;
import com.sap.cloud.yaas.servicesdk.jerseysupport.pagination.PaginationSupport;
import com.sap.wishlist.service.WishlistMediaService;
import com.sap.wishlist.service.WishlistService;


/**
 * Resource class containing the custom logic.
 */
@Component("apiWishlistsResource")
@Singleton
public class DefaultWishlistsResource implements WishlistsResource {

	@Context
	private UriInfo uriInfo;

	@Context
	private ContainerRequest request;

	@Inject
	private WishlistService wishlistService;

	@Inject
	private WishlistMediaService wishlistMediaService;

	/* GET / */
	@Override
	public Response get(final CountableParameters countable, final PagedParameters paged,
			final YaasAwareParameters yaasAware) {

		final PaginatedCollection<Wishlist> result = wishlistService
				.getWishlists(new PaginationRequest(paged.getPageNumber(), paged.getPageSize(), countable.isTotalCount()), yaasAware);

		final ResponseBuilder responseBuilder = Response.ok(result);
		PaginationSupport.decorateResponseWithCount(responseBuilder, result);
		PaginationSupport.decorateResponseWithPage(uriInfo, responseBuilder, result);
		return responseBuilder.build();
	}

	/* POST / */
	@Override
	public Response post(final YaasAwareParameters yaasAware, final Wishlist wishlist) {

		final String id = wishlistService.createWishlist(yaasAware, wishlist);

		final URI createdLocation = uriInfo.getRequestUriBuilder().path(id).build();
		return Response.created(createdLocation).build();
	}

	/* GET //{wishlistId} */
	@Override
	public Response getByWishlistId(final YaasAwareParameters yaasAware, final String wishlistId) {

		final Wishlist result = wishlistService.getWishlist(yaasAware, wishlistId);

		return Response.ok(result).build();
	}

	/* PUT //{wishlistId} */
	@Override
	public Response putByWishlistId(final YaasAwareParameters yaasAware, final String wishlistId, final Wishlist wishlist) {

		wishlistService.updateWishlist(yaasAware, wishlistId, wishlist);

		return Response.ok().build();
	}

	/* DELETE //{wishlistId} */
	@Override
	public Response deleteByWishlistId(final YaasAwareParameters yaasAware, final String wishlistId) {

		wishlistService.deleteWishlist(yaasAware, wishlistId);

		return Response.noContent().build();
	}

	/* POST //{wishlistId}/media */
	@Override
	public Response postByWishlistIdMedia(final YaasAwareParameters yaasAware, final String wishlistId) {

		final FormDataMultiPart multiPart = request.readEntity(FormDataMultiPart.class);
		final InputStream fileInputStream = ((BodyPartEntity) multiPart.getField("file").getEntity()).getInputStream();

		final String id = wishlistMediaService.createWishlistMedia(yaasAware,
				wishlistId, fileInputStream);

		final URI createdLocation = uriInfo.getRequestUriBuilder().path(id).build();
		final ResourceLocation rc = new ResourceLocation();
		rc.setId(id);
		rc.setLink(createdLocation);

		return Response.created(createdLocation).entity(rc).build();
	}

	/* GET //{wishlistId}/media */
	@Override
	public Response getByWishlistIdMedia(final CountableParameters countable, final PagedParameters paged,
			final YaasAwareParameters yaasAware, final String wishlistId) {

		final PaginatedCollection<WishlistMedia> result = wishlistMediaService.getWishlistMedias(
				new PaginationRequest(paged.getPageNumber(), paged.getPageSize(), countable.isTotalCount()), yaasAware, wishlistId);

		final ResponseBuilder responseBuilder = Response.ok(result);
		PaginationSupport.decorateResponseWithCount(responseBuilder, result);
		PaginationSupport.decorateResponseWithPage(uriInfo, responseBuilder, result);
		return responseBuilder.entity(result).build();
	}

	/* DELETE //{wishlistId}/media/{mediaId} */
	@Override
	public Response deleteByWishlistIdMediaByMediaId(final YaasAwareParameters yaasAware, final String wishlistId,
			final String mediaId) {

		wishlistMediaService.deleteWishlistMedia(yaasAware,
				wishlistId, mediaId);

		return Response.noContent().build();
	}

	/* GET //{wishlistId}/wishlistItems */
	@Override
	public Response getByWishlistIdWishlistItems(final PagedParameters paged, final YaasAwareParameters yaasAware,
			final String wishlistId) {

		final List<WishlistItem> result = wishlistService.getWishlistItems(
				new PaginationRequest(paged.getPageNumber(), paged.getPageSize(), false), yaasAware, wishlistId);

		return Response.ok().entity(result).build();
	}

	/* POST //{wishlistId}/wishlistItems */
	@Override
	public Response postByWishlistIdWishlistItems(final YaasAwareParameters yaasAware, final String wishlistId,
			final WishlistItem wishlistItem) {

		wishlistService.createWishlistItem(yaasAware, wishlistId, wishlistItem);

		return Response.created(uriInfo.getRequestUri()).build();
	}
}
