package com.sap.wishlist.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

import com.sap.cloud.yaas.servicesdk.jerseysupport.pagination.PaginatedCollection;
import com.sap.cloud.yaas.servicesdk.jerseysupport.pagination.PaginationRequest;
import com.sap.wishlist.api.generated.Wishlist;
import com.sap.wishlist.api.generated.WishlistItem;
import com.sap.wishlist.api.generated.YaasAwareParameters;
import com.sap.wishlist.customer.CustomerClientService;
import com.sap.wishlist.document.DocumentClientService;
import com.sap.wishlist.email.EmailClientService;
import com.sap.wishlist.utility.AuthorizationHelper;

@ManagedBean
public class WishlistService {

	private static final String SCOPE_DOCUMENT_VIEW = "hybris.document_view";
	private static final String SCOPE_DOCUMENT_MANAGE = "hybris.document_manage";
	private static final String SCOPE_CUSTOMER_VIEW = "hybris.customer_read";
	private static final String SCOPE_EMAIL_SEND = "hybris.email_send";
	private static final String SCOPE_EMAIL_MANAGE = "hybris.email_manage";

	@Inject
	private EmailClientService emailClient;
	@Inject
	private CustomerClientService customerClient;
	@Inject
	private DocumentClientService documentClient;
	@Inject
	private AuthorizationHelper authHelper;

	public PaginatedCollection<Wishlist> getWishlists(final PaginationRequest paginationRequest,
			final YaasAwareParameters yaasAware) {

		return authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_VIEW,
				token -> documentClient.getWishlists(paginationRequest, yaasAware, token));
	}

	public String createWishlist(final YaasAwareParameters yaasAware, final Wishlist wishlist) {

		final String email = authHelper.wrapWithAuthorization(yaasAware, SCOPE_CUSTOMER_VIEW,
				token -> customerClient.getCustomer(yaasAware, wishlist.getOwner(), token));

		final String createdId = authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_MANAGE,
				token -> {
					return documentClient.createWishlist(yaasAware, wishlist, token);
				});

		final boolean created = authHelper.wrapWithAuthorization(yaasAware, SCOPE_EMAIL_MANAGE,
				token -> emailClient.createTemplate(yaasAware, token));

		if (created) {
			authHelper.wrapWithAuthorization(yaasAware, SCOPE_EMAIL_MANAGE,
					token -> {
						emailClient.uploadTemplateSubject(yaasAware, token);
						return null;
					});

			authHelper.wrapWithAuthorization(yaasAware, SCOPE_EMAIL_MANAGE,
					token -> {
						emailClient.uploadTemplateBody(yaasAware, token);
						return null;
					});
		}

		authHelper.wrapWithAuthorization(yaasAware, SCOPE_EMAIL_SEND,
				token -> {
					emailClient.sendMail(yaasAware, wishlist, email, token);
					return null;
				});

		return createdId;
	}

	public Wishlist getWishlist(final YaasAwareParameters yaasAware, final String wishlistId) {

		return authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_VIEW,
				token -> documentClient.getWishlist(yaasAware, wishlistId, token));
	}

	public void updateWishlist(final YaasAwareParameters yaasAware,
			final String wishlistId, final Wishlist wishlist) {

		authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_MANAGE,
				token -> {
					documentClient.updateWishlist(yaasAware, wishlistId, wishlist, token);
					return null;
				});
	}

	public void deleteWishlist(final YaasAwareParameters yaasAware, final String wishlistId) {

		authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_MANAGE,
				token -> {
					documentClient.deleteWishlist(yaasAware, wishlistId, token);
					return null;
				});
	}

	public List<WishlistItem> getWishlistItems(final PaginationRequest paged, final YaasAwareParameters yaasAware,
			final String wishlistId) {

		return authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_VIEW,
				token -> documentClient.getWishlistItems(paged, yaasAware, wishlistId, token));
	}

	public void createWishlistItem(final YaasAwareParameters yaasAware, final String wishlistId,
			final WishlistItem wishlistItem) {

		final Wishlist wishlist = authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_VIEW,
				token -> documentClient.getWishlist(yaasAware, wishlistId, token));

		List<WishlistItem> wishlistItems = wishlist.getItems();
		if (wishlistItems != null) {
			wishlistItems.add(wishlistItem);
		} else {
			wishlistItems = new ArrayList<WishlistItem>();
			wishlistItems.add(wishlistItem);
		}
		wishlist.setItems(wishlistItems);
		authHelper.wrapWithAuthorization(yaasAware, SCOPE_DOCUMENT_MANAGE,
				token -> {
					documentClient.updateWishlist(yaasAware, wishlistId,
							wishlist, token);
					return null;
				});
	}
}
