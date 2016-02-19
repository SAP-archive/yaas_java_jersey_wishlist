package com.sap.wishlist.document;

import com.sap.wishlist.api.generated.WishlistMedia;


public class DocumentWishlistMedia {

	private String id;

	private String wishlistId;

	private DocumentMetaData metadata;

	private WishlistMedia wishlistMedia;

	public String getId() {
		return id;
	}

	public String getWishlistId() {
		return wishlistId;
	}

	public DocumentMetaData getMetadata() {
		return metadata;
	}

	public WishlistMedia getWishlistMedia() {
		return wishlistMedia;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setWishlistId(final String wishlistId) {
		this.wishlistId = wishlistId;
	}

	public void setMetadata(final DocumentMetaData metadata) {
		this.metadata = metadata;
	}

	public void setWishlistMedia(final WishlistMedia wishlistMedia) {
		this.wishlistMedia = wishlistMedia;
	}
}
