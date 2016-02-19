package com.sap.wishlist.utility;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.sap.cloud.yaas.servicesdk.jerseysupport.features.JsonFeature;
import com.sap.cloud.yaas.servicesdk.jerseysupport.logging.RequestResponseLoggingFilter;


public class ClientFactory implements FactoryBean<Client> {

	private static final Logger LOG = LoggerFactory.getLogger(ClientFactory.class);

	@Override
	public Client getObject() throws Exception {
		return ClientBuilder.newBuilder()
				.register(new JsonFeature())
				.register(new RequestResponseLoggingFilter(LOG, 1000))
				.register(MultiPartFeature.class)
				.build();
	}

	@Override
	public Class<?> getObjectType() {
		return Client.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
