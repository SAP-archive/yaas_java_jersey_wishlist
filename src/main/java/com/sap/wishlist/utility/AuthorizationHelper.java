package com.sap.wishlist.utility;

import java.util.Arrays;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;

import com.sap.cloud.yaas.servicesdk.authorization.AuthorizationScope;
import com.sap.cloud.yaas.servicesdk.authorization.DiagnosticContext;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionCallback;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;
import com.sap.wishlist.api.generated.YaasAwareParameters;


@ManagedBean
public class AuthorizationHelper {

	@Value("${YAAS_CLIENT_IS_APPLICATION}")
	private Boolean isSingleTenant;

	@Inject
	private AuthorizedExecutionTemplate authorizedExecutionTemplate;

	public <T> T wrapWithAuthorization(final YaasAwareParameters context, final String scope,
			final AuthorizedExecutionCallback<T> callback) {

		return authorizedExecutionTemplate.executeAuthorized(
				createAuthorizationScope(context.getHybrisTenant(), Arrays.asList(scope)),
				new DiagnosticContext(context.getHybrisRequestId(), context.getHybrisHop()),
				callback);
	}

	public AuthorizationScope createAuthorizationScope(final String tenant, final List<String> scopes) {

		if (isSingleTenant) {
			return new AuthorizationScope(scopes);
		} else {
			return new AuthorizationScope(tenant, scopes);
		}

	}
}
