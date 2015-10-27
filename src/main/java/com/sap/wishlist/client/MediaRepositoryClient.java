package com.sap.wishlist.client;

import javax.annotation.ManagedBean;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.sap.wishlist.client.mediaRepository.MediaClient;

@ManagedBean
public class MediaRepositoryClient extends MediaClient {
	public MediaRepositoryClient() {
		super(DEFAULT_BASE_URI, ClientBuilder.newClient().register(
				MultiPartFeature.class));
	}
}