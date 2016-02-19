package com.sap.wishlist.customer;

import java.util.Map;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.wishlist.api.generated.YaasAwareParameters;
import com.sap.wishlist.client.customer.CustomerServiceClient;
import com.sap.wishlist.utility.ErrorHandler;


@ManagedBean
public class CustomerClientService {

	private static final String ATT_CONTACT_EMAIL = "contactEmail";

	@Inject
	private CustomerServiceClient customerClient;

	public String getCustomer(final YaasAwareParameters yaasAware, final String customer, final AccessToken token) {
		final Response response = customerClient
				.tenant(yaasAware.getHybrisTenant())
				.customers()
				.customerNumber(customer)
				.prepareGet()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.execute();

		if (response.getStatus() == Status.OK.getStatusCode()) {
			return (String) response.readEntity(Map.class).get(ATT_CONTACT_EMAIL);
		} else if (response.getStatus() == Status.NOT_FOUND.getStatusCode()) {
			throw new BadRequestException("Owner " + customer + " does not exist");
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}
}
