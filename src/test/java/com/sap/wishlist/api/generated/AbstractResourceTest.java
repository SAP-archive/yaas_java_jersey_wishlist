package com.sap.wishlist.api.generated;


import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.validation.ValidationFeature;

public abstract class AbstractResourceTest extends JerseyTest
{
	protected WebTarget getRootTarget(final String rootResource)
	{
		return client().target(getBaseUri()).path(rootResource);
	}

	@Override
	protected final Application configure()
	{
		final ResourceConfig application = configureApplication();

		// needed for json serialization
		application.register(JacksonFeature.class);

		// bean validation
		application.register(ValidationFeature.class);

		// configure spring context
		application.property("contextConfigLocation", "classpath:/META-INF/applicationContext.xml");

		// disable bean validation for tests
		application.property(ServerProperties.BV_FEATURE_DISABLE, "true");

		return application;
	}

	protected abstract ResourceConfig configureApplication();

	@Override
	protected void configureClient(final ClientConfig config)
	{
		// needed for json serialization
		config.register(JacksonFeature.class);

		config.register(new LoggingFilter(java.util.logging.Logger.getLogger(AbstractResourceTest.class.getName()), false));

		super.configureClient(config);
	}
}
