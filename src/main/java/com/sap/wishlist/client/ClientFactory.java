package com.sap.wishlist.client;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.springframework.beans.factory.FactoryBean;

import com.sap.cloud.yaas.servicesdk.jerseysupport.features.JsonFeature;

public class ClientFactory implements FactoryBean<Client> {

	@Override
	public Client getObject() throws Exception {
		Client client = ClientBuilder.newBuilder().register(new JsonFeature())
				.build();
		return client;
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
