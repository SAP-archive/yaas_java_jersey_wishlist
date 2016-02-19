package com.sap.wishlist.service;

import java.io.InputStream;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

import com.sap.cloud.yaas.servicesdk.jerseysupport.pagination.PaginatedCollection;
import com.sap.cloud.yaas.servicesdk.jerseysupport.pagination.PaginationRequest;
import com.sap.cloud.yaas.servicesdk.patternsupport.schemas.ResourceLocation;
import com.sap.wishlist.api.generated.WishlistMedia;
import com.sap.wishlist.api.generated.YaasAwareParameters;
import com.sap.wishlist.document.DocumentClientService;
import com.sap.wishlist.media.MediaClientService;
import com.sap.wishlist.utility.AuthorizationHelper;


@ManagedBean
public class WishlistMediaService {

	private static final String SCOPE_DOCUMENT_VIEW = "hybris.document_view";
	private static final String SCOPE_DOCUMENT_MANAGE = "hybris.document_manage";
	private static final String SCOPE_MEDIA_MANAGE = "hybris.media_manage";

	@Inject
	private MediaClientService mediaClient;
	@Inject
	private DocumentClientService documentClient;
	@Inject
	private AuthorizationHelper authHelper;

	public String createWishlistMedia(final YaasAwareParameters yaasAware, final String wishlistId,
			final InputStream fileInputStream) {

		final ResourceLocation location = authHelper.wrapWithAuthorization(yaasAware, SCOPE_MEDIA_MANAGE,
				token -> mediaClient.createMedia(yaasAware, fileInputStream, token));

		return authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_MANAGE,
				token -> documentClient.createWishlistMedia(yaasAware, wishlistId, location.getId(), location.getLink(), token));
	}

	public PaginatedCollection<WishlistMedia> getWishlistMedias(final PaginationRequest paginationRequest,
			final YaasAwareParameters yaasAware, final String wishlistId) {

		return authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_VIEW,
				token -> documentClient.getWishlistMedias(yaasAware, paginationRequest, wishlistId, token));
	}

	public void deleteWishlistMedia(final YaasAwareParameters yaasAware, @SuppressWarnings("unused") final String wishlistId,
			final String mediaId) {

		final WishlistMedia media = authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_VIEW,
				token -> documentClient.getWishlistMedia(yaasAware, mediaId, token));

		authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_MANAGE,
				token -> {
					documentClient.deleteWishlistMedia(yaasAware, mediaId, token);
					return null;
				});

		authHelper.wrapWithAuthorization(yaasAware, SCOPE_MEDIA_MANAGE,
				token -> {
					mediaClient.deleteMedia(yaasAware, media.getId(), token);
					return null;
				});
	}
}
