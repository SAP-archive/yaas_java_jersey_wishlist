package com.sap.wishlist.api.generated;


import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.slf4j.LoggerFactory;

import com.sap.cloud.yaas.servicesdk.jerseysupport.features.JsonFeature;
import com.sap.cloud.yaas.servicesdk.jerseysupport.logging.RequestResponseLoggingFilter;
import com.sap.wishlist.JerseyApplication;


public abstract class AbstractResourceTest extends JerseyTest {
	protected WebTarget getRootTarget(final String rootResource) {
		return client().target(getBaseUri()).path(rootResource);
	}

	@Override
	protected final Application configure() {
		final ResourceConfig application = new JerseyApplication();

		// configure spring context
		application.property("contextConfigLocation", "classpath:/META-INF/applicationContext.xml");

		return application;
	}

	protected abstract ResourceConfig configureApplication();

	@Override
	protected void configureClient(final ClientConfig config) {

		config.register(JsonFeature.class);
		config.register(new RequestResponseLoggingFilter(LoggerFactory.getLogger(AbstractResourceTest.class), 1000));
		config.register(MultiPartFeature.class);

		super.configureClient(config);
	}
}
