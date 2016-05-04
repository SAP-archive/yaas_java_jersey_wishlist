package com.sap.wishlist.document;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Value;

import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.jerseysupport.pagination.PaginatedCollection;
import com.sap.cloud.yaas.servicesdk.jerseysupport.pagination.PaginationRequest;
import com.sap.cloud.yaas.servicesdk.patternsupport.schemas.ResourceLocation;
import com.sap.wishlist.api.generated.Wishlist;
import com.sap.wishlist.api.generated.WishlistItem;
import com.sap.wishlist.api.generated.WishlistMedia;
import com.sap.wishlist.api.generated.YaasAwareParameters;
import com.sap.wishlist.client.document.DocumentClient;
import com.sap.wishlist.utility.ErrorHandler;

@ManagedBean
public class DocumentClientService {
	private static final String WISHLIST_PATH = "wishlist";
	private static final String WISHLIST_MEDIA_PATH = "wishlistMedia";

	@Value("${YAAS_CLIENT}")
	private String client;

	@Inject
	private DocumentClient documentClient;

	public PaginatedCollection<Wishlist> getWishlists(final PaginationRequest paginationRequest,
			final YaasAwareParameters yaasAware, final AccessToken token) {

		final Response response = documentClient.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_PATH)
				.prepareGet()
				.withPageNumber(paginationRequest.getPageNumber())
				.withPageSize(paginationRequest.getPageSize())
				.withTotalCount(paginationRequest.isCountingTotal())
				.withAuthorization(token.toAuthorizationHeaderValue())
				.execute();

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			final List<Wishlist> wishlists = Arrays.stream(response.readEntity(DocumentWishlist[].class))
					.map(document -> transformWishlist(document))
					.collect(Collectors.toList());

			return PaginatedCollection.<Wishlist> of(wishlists)
					.with(response, paginationRequest)
					.build();
		} else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			return PaginatedCollection.of(Collections.<Wishlist> emptyList()).build();
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	private Wishlist transformWishlist(final DocumentWishlist documentWishlist) {
		final Wishlist result = documentWishlist.getWishlist();
		result.setCreatedAt(documentWishlist.getMetadata().getCreatedAt());
		return result;
	}

	public String createWishlist(final YaasAwareParameters yaasAware, final Wishlist wishlist, final AccessToken token) {
		final DocumentWishlist documentWishlist = new DocumentWishlist();
		documentWishlist.setWishlist(wishlist);

		final Response response = documentClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_PATH)
				.dataId(wishlist.getId())
				.preparePost()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.withPayload(Entity.json(documentWishlist))
				.execute();

		if (response.getStatus() == Status.CREATED.getStatusCode()) {
			return response.readEntity(ResourceLocation.class).getId();
		} else if (response.getStatus() == Status.CONFLICT.getStatusCode()) {
			throw new WebApplicationException("Duplicate ID. Please provide another ID for the wishlist.", response);
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public Wishlist getWishlist(final YaasAwareParameters yaasAware, final String id, final AccessToken token) {

		final Response response = documentClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_PATH)
				.dataId(id)
				.prepareGet()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.execute();

		if (response.getStatus() == Status.OK.getStatusCode()) {
			final DocumentWishlist documentWishlist = response.readEntity(DocumentWishlist.class);
			final Wishlist wishlist = documentWishlist.getWishlist();
			wishlist.setCreatedAt(documentWishlist.getMetadata().getCreatedAt());
			return wishlist;

		} else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			throw new NotFoundException("Cannot find wishlist with ID " + id, response);
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public void updateWishlist(final YaasAwareParameters yaasAware, final String wishlistId, final Wishlist wishlist,
			final AccessToken token) {

		final DocumentWishlist documentWishlist = new DocumentWishlist();
		documentWishlist.setWishlist(wishlist);

		final Response response = documentClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_PATH)
				.dataId(wishlistId)
				.preparePut()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.withPayload(Entity.json(documentWishlist))
				.execute();

		if (response.getStatus() == Status.OK.getStatusCode()) {
			return;
		} else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			throw new NotFoundException("Cannot find wishlist with ID " + wishlistId, response);
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public void deleteWishlist(final YaasAwareParameters yaasAware, final String wishlistId, final AccessToken token) {
		final Response response = documentClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_PATH)
				.dataId(wishlistId)
				.prepareDelete()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.execute();

		if (response.getStatus() == Status.NO_CONTENT.getStatusCode()) {
			return;
		} else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			throw new NotFoundException("Cannot find wishlist with ID " + wishlistId, response);
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public String createWishlistMedia(final YaasAwareParameters yaasAware, final String wishlistId,
			final String mediaId,
			final URI location, final AccessToken token) {

		final WishlistMedia wishlistMedia = new WishlistMedia();
		wishlistMedia.setId(mediaId);
		wishlistMedia.setUri(location);

		final DocumentWishlistMedia documentWishlistMedia = new DocumentWishlistMedia();
		documentWishlistMedia.setWishlistId(wishlistId);
		documentWishlistMedia.setWishlistMedia(wishlistMedia);

		final Response response = documentClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_MEDIA_PATH)
				.preparePost()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.withPayload(documentWishlistMedia)
				.execute();

		if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
			return response.readEntity(ResourceLocation.class).getId();
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public PaginatedCollection<WishlistMedia> getWishlistMedias(final YaasAwareParameters yaasAware,
			final PaginationRequest paginationRequest, final String wishlistId, final AccessToken token) {

		final Response response = documentClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_MEDIA_PATH)
				// get only the media with wishlistId
				.withQuery("q", "wishlistId:" + wishlistId)
				.prepareGet()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.withTotalCount(paginationRequest.isCountingTotal())
				.withPageNumber(paginationRequest.getPageNumber())
				.withPageSize(paginationRequest.getPageSize()).execute();

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			final List<WishlistMedia> medias = Arrays.stream(response
					.readEntity(DocumentWishlistMedia[].class))
					.map(document -> transformWishlistMedia(document))
					.collect(Collectors.toList());

			return PaginatedCollection.<WishlistMedia> of(medias)
					.with(response, paginationRequest)
					.build();
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	private WishlistMedia transformWishlistMedia(final DocumentWishlistMedia media) {
		final WishlistMedia result = new WishlistMedia();
		result.setId(media.getId());
		result.setUri(media.getWishlistMedia().getUri());
		return result;
	}

	public WishlistMedia getWishlistMedia(final YaasAwareParameters yaasAware, final String mediaId,
			final AccessToken token) {
		final Response response = documentClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_MEDIA_PATH)
				.dataId(mediaId)
				.prepareGet()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.execute();

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			final DocumentWishlistMedia documentWishlistMedia = response.readEntity(DocumentWishlistMedia.class);
			final WishlistMedia wishlistMedia = new WishlistMedia();
			wishlistMedia.setId(documentWishlistMedia.getWishlistMedia().getId());
			wishlistMedia.setUri(documentWishlistMedia.getWishlistMedia().getUri());
			return wishlistMedia;
		} else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			throw new NotFoundException("Cannot find wishlist media with ID " + mediaId, response);
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public void deleteWishlistMedia(final YaasAwareParameters yaasAware, final String mediaId, final AccessToken token) {
		final Response response = documentClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_MEDIA_PATH)
				.dataId(mediaId)
				.prepareDelete()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.execute();

		if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
			return;
		} else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			throw new NotFoundException("Cannot find wishlist media with ID " + mediaId, response);
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public List<WishlistItem> getWishlistItems(final PaginationRequest paged, final YaasAwareParameters yaasAware,
			final String wishlistId, final AccessToken token) {

		final Response response = documentClient
				.tenant(yaasAware.getHybrisTenant())
				.client(client)
				.dataType(WISHLIST_PATH)
				.dataId(wishlistId)
				.prepareGet()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.execute();

		if (response.getStatus() == Status.OK.getStatusCode()) {
			final Wishlist wishlist = response.readEntity(DocumentWishlist.class).getWishlist();
			final List<WishlistItem> wishlistItems = wishlist.getItems();

			if (wishlistItems == null) {
				return Collections.emptyList();
			}
			final int lastIndexOfItems = wishlistItems.size() - 1;
			final int pageNum = paged.getPageNumber();
			final int pageSize = paged.getPageSize();

			final int indexOfStartItem = pageNum * pageSize - pageSize;
			final int indexOfEndItem = pageNum * pageSize - 1;

			final int min = Math.min(indexOfEndItem, lastIndexOfItems);

			return IntStream.range(indexOfStartItem, min + 1)
					.mapToObj(i -> wishlistItems.get(i))
					.collect(Collectors.toList());
		} else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			return Collections.emptyList();
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}
}
