package com.sap.wishlist.service;

import java.io.File;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Value;

import com.sap.cloud.yaas.servicesdk.authorization.AccessToken;
import com.sap.cloud.yaas.servicesdk.authorization.DiagnosticContext;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionCallback;
import com.sap.cloud.yaas.servicesdk.authorization.integration.AuthorizedExecutionTemplate;
import com.sap.wishlist.api.generated.Customer;
import com.sap.wishlist.api.generated.DocumentWishlist;
import com.sap.wishlist.api.generated.DocumentWishlistRead;
import com.sap.wishlist.api.generated.Error;
import com.sap.wishlist.api.generated.PagedParameters;
import com.sap.wishlist.api.generated.ResourceLocation;
import com.sap.wishlist.api.generated.Wishlist;
import com.sap.wishlist.api.generated.YaasAwareParameters;
import com.sap.wishlist.client.customer.CustomerServiceClient;
import com.sap.wishlist.client.documentrepository.DocumentClient;
import com.sap.wishlist.client.email.EmailServiceClient;
import com.sap.wishlist.email.Email;
import com.sap.wishlist.email.EmailTemplate;
import com.sap.wishlist.email.EmailTemplateDefinition;
import com.sap.wishlist.email.TemplateAttributeDefinition;
import com.sap.wishlist.email.TemplateAttributeValue;
import com.sap.wishlist.utility.AuthorizationHelper;
import com.sap.wishlist.utility.ErrorHandler;

@ManagedBean
public class WishlistService {

	public static final String WISHLIST_PATH = "wishlist";

	@Inject
	private EmailServiceClient emailClient;
	@Inject
	private CustomerServiceClient customerClient;
	@Inject
	private DocumentClient documentClient;
	@Inject
	private AuthorizedExecutionTemplate authorizedExecutionTemplate;
	@Inject
	private AuthorizationHelper authorizationHelper;
	@Value("${YAAS_CLIENT}")
	private String client;

	private final String TEMPLATE_CODE = "wishlist";

	/* GET / */
	public Response get(final PagedParameters paged,
			final YaasAwareParameters yaasAware) {
		ArrayList<Wishlist> result = null;
		Response response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return documentClient
								.tenant(yaasAware.getHybrisTenant())
								.clientData(client)
								.type(WISHLIST_PATH)
								.prepareGet()
								.withPageNumber(paged.getPageNumber())
								.withPageSize(paged.getPageSize())
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.execute();
					}
				});
		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			result = new ArrayList<Wishlist>();
			for (DocumentWishlistRead documentWishlist : response
					.readEntity(DocumentWishlistRead[].class)) {
				Wishlist wishlist = documentWishlist.getWishlist();

				String dateString = documentWishlist.getMetadata()
						.getCreatedAt();
				SimpleDateFormat df = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				Date createdAt;
				try {
					createdAt = df.parse(dateString);
					wishlist.setCreatedAt(createdAt);
				} catch (ParseException e) {
					e.printStackTrace();
					throw new InternalServerErrorException();
				}
				result.add(wishlist);
			}

		} else {
			ErrorHandler.handleResponse(response);
		}

		return Response.ok().entity(result).build();
	}

	/* POST / */
	public Response post(final YaasAwareParameters yaasAware,
			final UriInfo uriInfo, final Wishlist wishlist) {
		final DocumentWishlist documentWishlist = new DocumentWishlist();
		documentWishlist.setWishlist(wishlist);

		// Check if Customer exist
		Response responseCustomer = authorizedExecutionTemplate
				.executeAuthorized(authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
						new DiagnosticContext(yaasAware.getHybrisRequestId(),
								yaasAware.getHybrisHop()),
						new AuthorizedExecutionCallback<Response>() {
							@Override
							public Response execute(final AccessToken token) {
								return customerClient
										.tenant(yaasAware.getHybrisTenant())
										.customers()
										.customerNumber(wishlist.getOwner())
										.prepareGet()
										.withAuthorization(
												authorizationHelper
														.buildToken(token))
										.execute();
							}
						});

		if (responseCustomer.getStatus() == Status.OK.getStatusCode()) {
			Customer customer = responseCustomer.readEntity(Customer.class);

			Response response = authorizedExecutionTemplate.executeAuthorized(
					authorizationHelper.getAuthorizationScope(
							yaasAware.getHybrisTenant(),
							authorizationHelper.getScopes()),
					new DiagnosticContext(yaasAware.getHybrisRequestId(),
							yaasAware.getHybrisHop()),
					new AuthorizedExecutionCallback<Response>() {
						@Override
						public Response execute(final AccessToken token) {
							return documentClient
									.tenant(yaasAware.getHybrisTenant())
									.clientData(client)
									.type(WISHLIST_PATH)
									.dataId(wishlist.getId())
									.preparePost()
									.withAuthorization(
											authorizationHelper
													.buildToken(token))
									.withPayload(Entity.json(documentWishlist))
									.execute();
						}
					});

			if (response.getStatus() != Status.CREATED.getStatusCode()) {
				if (response.getStatus() == Status.CONFLICT.getStatusCode()) {
					Error err = new Error();
					err.setStatus(Status.CONFLICT.getStatusCode());
					err.setMessage("Duplicate ID. Please provide another ID for the wishlist.");
					return Response.status(Status.CONFLICT.getStatusCode())
							.entity(err).type(MediaType.APPLICATION_JSON)
							.build();
				} else {
					ErrorHandler.handleResponse(response);
					return null;
				}
			} else {
				sendMail(yaasAware, wishlist, customer.getContactEmail());
				ResourceLocation location = response
						.readEntity(ResourceLocation.class);
				URI createdLocation = uriInfo.getRequestUriBuilder()
						.path("/" + location.getId()).build();
				return Response.created(createdLocation).build();
			}
		} else {
			Error err = new Error();
			err.setStatus(Status.BAD_REQUEST.getStatusCode());
			err.setMessage("Owner does not exist");
			return Response.status(Status.BAD_REQUEST.getStatusCode())
					.entity(err).type(MediaType.APPLICATION_JSON).build();
		}
	}

	/* GET //{wishlistId} */
	public Response getByWishlistId(final YaasAwareParameters yaasAware,
			final java.lang.String wishlistId) {
		Response response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return documentClient
								.tenant(yaasAware.getHybrisTenant())
								.clientData(client)
								.type(WISHLIST_PATH)
								.dataId(wishlistId)
								.prepareGet()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.execute();
					}
				});

		if (response.getStatus() != Status.OK.getStatusCode()) {
			ErrorHandler.handleResponse(response);
		}

		DocumentWishlistRead documentWishlistRead = response
				.readEntity(DocumentWishlistRead.class);
		return Response.ok(documentWishlistRead.getWishlist()).build();
	}

	/* PUT //{wishlistId} */
	public Response putByWishlistId(final YaasAwareParameters yaasAware,
			final java.lang.String wishlistId, final Wishlist wishlist) {
		final DocumentWishlist documentWishlist = new DocumentWishlist();
		documentWishlist.setWishlist(wishlist);

		Response response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return documentClient
								.tenant(yaasAware.getHybrisTenant())
								.clientData(client)
								.type(WISHLIST_PATH)
								.dataId(wishlist.getId())
								.preparePut()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.withPayload(Entity.json(documentWishlist))
								.execute();
					}
				});

		if (response.getStatus() != Status.OK.getStatusCode()) {
			ErrorHandler.handleResponse(response);
		}

		return Response.ok().build();
	}

	/* DELETE //{wishlistId} */
	public Response deleteByWishlistId(final YaasAwareParameters yaasAware,
			final java.lang.String wishlistId) {
		Response response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return documentClient
								.tenant(yaasAware.getHybrisTenant())
								.clientData(client)
								.type(WISHLIST_PATH)
								.dataId(wishlistId)
								.prepareDelete()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.execute();
					}
				});

		if (response.getStatus() != Status.NO_CONTENT.getStatusCode()) {
			ErrorHandler.handleResponse(response);
		}

		return Response.noContent().build();
	}

	private boolean sendMail(final YaasAwareParameters yaasAware,
			final Wishlist wishlist, final String mail) {
		// Create Email Template if not exist
		final EmailTemplateDefinition emailTemplateDefinition = new EmailTemplateDefinition();
		emailTemplateDefinition.setCode(TEMPLATE_CODE);
		emailTemplateDefinition.setOwner(this.client);
		emailTemplateDefinition.setName("Wishlist Created Mail");
		emailTemplateDefinition
				.setDescription("Template for Wishlist Created Mail");

		List<TemplateAttributeDefinition> templateAttributeDefinition = new ArrayList<TemplateAttributeDefinition>();
		templateAttributeDefinition.add(new TemplateAttributeDefinition(
				"title", false));
		templateAttributeDefinition.add(new TemplateAttributeDefinition(
				"description", false));
		emailTemplateDefinition
				.setTemplateAttributeDefinitions(templateAttributeDefinition);

		Response response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return emailClient
								.tenantTemplates(yaasAware.getHybrisTenant())
								.preparePost()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.withPayload(
										Entity.json(emailTemplateDefinition))
								.execute();
					}
				});

		if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
			EmailTemplate templateSubject = EmailTemplate.builder()
					.setFilePath("templates" + File.separator + "subject.vm")
					.setCode(TEMPLATE_CODE)
					.setOwner(yaasAware.getHybrisTenant())
					.setFileType("subject").setLocale("en").build();
			uploadTemplate(yaasAware, templateSubject);

			EmailTemplate templateBody = EmailTemplate.builder()
					.setFilePath("templates" + File.separator + "body.vm")
					.setCode(TEMPLATE_CODE)
					.setOwner(yaasAware.getHybrisTenant()).setFileType("body")
					.setLocale("en").build();
			uploadTemplate(yaasAware, templateBody);

		}

		// Send Mail
		Email eMail = new Email();
		eMail.setToAddress(mail);
		eMail.setFromAddress("noreply@cf.hybris.com");
		eMail.setTemplateOwner(this.client);
		eMail.setTemplateCode(TEMPLATE_CODE);
		eMail.setLocale("en");

		List<TemplateAttributeValue> templateAttributeValue = new ArrayList<TemplateAttributeValue>();
		templateAttributeValue.add(new TemplateAttributeValue("title", wishlist
				.getTitle()));
		templateAttributeValue.add(new TemplateAttributeValue("description",
				wishlist.getDescription()));
		eMail.setAttributes(templateAttributeValue);

		response = authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return emailClient
								.tenantSend(yaasAware.getHybrisTenant())
								.preparePost()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.withPayload(Entity.json(eMail)).execute();
					}
				});

		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			return false;
		}
		return true;
	}

	private Response uploadTemplate(final YaasAwareParameters yaasAware,
			final EmailTemplate template) {
		final String client = this.client;

		return authorizedExecutionTemplate.executeAuthorized(
				authorizationHelper.getAuthorizationScope(
						yaasAware.getHybrisTenant(),
						authorizationHelper.getScopes()),
				new DiagnosticContext(yaasAware.getHybrisRequestId(), yaasAware
						.getHybrisHop()),
				new AuthorizedExecutionCallback<Response>() {
					@Override
					public Response execute(final AccessToken token) {
						return emailClient
								.tenantTemplatesClient(
										yaasAware.getHybrisTenant(), client)
								.code(template.getCode())
								.fileType(template.getFileType())
								.preparePut()
								.withAuthorization(
										authorizationHelper.buildToken(token))
								.withPayload(
										Entity.entity(
												template.getDataStream(),
												MediaType.APPLICATION_OCTET_STREAM_TYPE))
								.execute();
					}
				});
	}
}
