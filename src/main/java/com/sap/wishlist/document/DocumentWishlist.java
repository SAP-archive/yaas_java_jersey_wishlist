package com.sap.wishlist.document;

import com.sap.wishlist.api.generated.Wishlist;


public class DocumentWishlist {

	private Wishlist wishlist;

	private DocumentMetaData metadata;

	public DocumentMetaData getMetadata() {
		return metadata;
	}

	public void setMetadata(final DocumentMetaData metadata) {
		this.metadata = metadata;
	}

	public Wishlist getWishlist() {
		return wishlist;
	}

	public void setWishlist(final Wishlist wishlist) {
		this.wishlist = wishlist;
	}
}
