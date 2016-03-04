package com.sap.wishlist.email;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;

import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.wishlist.api.generated.Wishlist;
import com.sap.wishlist.api.generated.YaasAwareParameters;
import com.sap.wishlist.client.email.EmailServiceClient;
import com.sap.wishlist.utility.ErrorHandler;


@ManagedBean
public class EmailClientService {

	private final String TEMPLATE_CODE = "wishlist";
	private final String locale = Locale.ENGLISH.getLanguage();

	@Value("${YAAS_CLIENT}")
	private String client;

	@Inject
	private EmailServiceClient emailClient;

	public boolean createTemplate(final YaasAwareParameters yaasAware, final AccessToken token) {

		final EmailTemplateDefinition emailTemplateDefinition = new EmailTemplateDefinition();
		emailTemplateDefinition.setCode(TEMPLATE_CODE);
		emailTemplateDefinition.setOwner(client);
		emailTemplateDefinition.setName("Wishlist Created Mail");
		emailTemplateDefinition.setDescription("Template for Wishlist Created Mail");

		final List<TemplateAttributeDefinition> templateAttributeDefinition = new ArrayList<TemplateAttributeDefinition>();
		templateAttributeDefinition.add(new TemplateAttributeDefinition("title", false));
		templateAttributeDefinition.add(new TemplateAttributeDefinition("description", false));
		emailTemplateDefinition.setTemplateAttributeDefinitions(templateAttributeDefinition);

		final Response response = emailClient
				.tenantTemplates(yaasAware.getHybrisTenant())
				.preparePost()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.withPayload(Entity.json(emailTemplateDefinition))
				.execute();

		if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
			return true;
		} else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
			return false;
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public void uploadTemplateSubject(final YaasAwareParameters yaasAware, final AccessToken token) {
		uploadTemplateMedia(yaasAware, "subject", token);
	}

	public void uploadTemplateBody(final YaasAwareParameters yaasAware, final AccessToken token) {
		uploadTemplateMedia(yaasAware, "body", token);
	}

	private void uploadTemplateMedia(final YaasAwareParameters yaasAware, final String type, final AccessToken token) {
		final EmailTemplate template = EmailTemplate.builder()
				.setFilePath("templates" + File.separator + type + ".vm")
				.setCode(TEMPLATE_CODE)
				.setOwner(yaasAware.getHybrisTenant())
				.setFileType(type)
				.setLocale(locale)
				.build();

		final Response response = emailClient
				.tenantTemplatesClient(yaasAware.getHybrisTenant(), client)
				.code(template.getCode())
				.fileType(template.getFileType())
				.preparePut()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.withPayload(Entity.entity(template.getDataStream(), MediaType.APPLICATION_OCTET_STREAM_TYPE))
				.execute();

		if (response.getStatus() == Response.Status.CREATED.getStatusCode() || response.getStatus() == Response.Status.OK.getStatusCode()) {
			return;
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}

	public void sendMail(final YaasAwareParameters yaasAware, final Wishlist wishlist, final String mail,
			final AccessToken token) {

		final Email eMail = new Email();
		eMail.setToAddress(mail);
		eMail.setFromAddress("noreply@" + yaasAware.getHybrisTenant() + ".mail.yaas.io");
		eMail.setTemplateOwner(client);
		eMail.setTemplateCode(TEMPLATE_CODE);
		eMail.setLocale(locale);

		final List<TemplateAttributeValue> templateAttributeValue = new ArrayList<TemplateAttributeValue>();
		if (wishlist.getTitle() != null) {
			templateAttributeValue.add(new TemplateAttributeValue("title", wishlist.getTitle()));
		}
		if (wishlist.getDescription() != null) {
			templateAttributeValue.add(new TemplateAttributeValue("description", wishlist.getDescription()));
		}
		eMail.setAttributes(templateAttributeValue);

		final Response response = emailClient
				.tenantSendAsync(yaasAware.getHybrisTenant())
				.preparePost()
				.withAuthorization(token.toAuthorizationHeaderValue())
				.withPayload(Entity.json(eMail)).execute();

		if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
			return;
		}
		throw ErrorHandler.resolveErrorResponse(response, token);
	}
}
