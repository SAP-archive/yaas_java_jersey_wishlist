package com.sap.wishlist.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.ManagedBean;

import org.springframework.beans.factory.annotation.Value;

import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.authorization.AuthorizationScope;

@ManagedBean
public class AuthorizationHelper {
	@Value("${OAUTH2_SCOPES}")
	private String scopes;
	@Value("${YAAS_CLIENT_IS_APPLICATION}")
	private Boolean isSingleTenant;

	public List<String> getScopes() {
		return new ArrayList<String>(Arrays.asList(this.scopes.split(" ")));
	}

	public String buildToken(AccessToken token) {
		return token.getType() + " " + token.getValue();
	}

	public AuthorizationScope getAuthorizationScope(final String tenant,
			final Collection<String> scopes) {
		if (isSingleTenant) {
			return new AuthorizationScope(scopes);
		} else {
			return new AuthorizationScope(tenant, scopes);
		}

	}
}