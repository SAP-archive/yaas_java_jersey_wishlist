package com.sap.wishlist.api.generated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sap.cloud.yaas.servicesdk.patternsupport.traits.YaasAwareTrait;
import com.sap.wishlist.api.TestConstants;
import com.sap.wishlist.service.WishlistMediaService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/META-INF/applicationContext.xml")
public final class DefaultWishlistsResourceTest extends AbstractResourceTest {
	/**
	 * Server side root resource /wishlists
	 */
	private static final String ROOT_RESOURCE_PATH = "/wishlists";
	private static final String REQUEST_URI = "https://local/wishlists";
	private static final String CLIENT = "test";
	private static final String TEST_FILE_FOR_UPLOAD = "src/test/resources/800x600.png";

	private static Wishlist wishlist;

	private ArrayList<String> instanceList = new ArrayList<String>();
	private ArrayList<String> instanceListMedia = new ArrayList<String>();

	@Inject
	private WishlistMediaService cut;

	private YaasAwareParameters yaasAware;

	@Before
	public void before() {
		this.yaasAware = new YaasAwareParameters();
		this.yaasAware.setHybrisClient(CLIENT);
		this.yaasAware.setHybrisTenant(TestConstants.TENANT);

		wishlist = new Wishlist();
		wishlist.setId(UUID.randomUUID().toString());
		wishlist.setDescription("Test");
		wishlist.setOwner(TestConstants.CUSTOMER);

		instanceList.add(wishlist.getId());

		createWishlist(wishlist);
	}

	/* get() /wishlists */
	@Test
	public void testGet() {
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path("");

		final Response response = target.request()
				.header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.get();

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("Response does not have expected response code",
				Status.OK.getStatusCode(), response.getStatus());
	}

	/* post(entity) /wishlists */
	@Test
	public void testPostWithWishlist() {
		Wishlist wishlist = new Wishlist();
		wishlist.setId(UUID.randomUUID().toString());
		wishlist.setOwner(TestConstants.CUSTOMER);
		instanceList.add(wishlist.getId());

		final Response response = createWishlist(wishlist);

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("Response does not have expected response code",
				Status.CREATED.getStatusCode(), response.getStatus());
	}

	/* post(entity) /wishlists */
	@Test
	public void testPostCheckDuplicateID() {
		Wishlist wishlist = new Wishlist();
		wishlist.setId(UUID.randomUUID().toString());
		wishlist.setOwner(TestConstants.CUSTOMER);
		instanceList.add(wishlist.getId());
		createWishlist(wishlist);

		final Response response = createWishlist(wishlist);

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals(
				"Should return conflict when wishlist id is already used",
				Status.CONFLICT.getStatusCode(), response.getStatus());
	}

	/* post(entity) /wishlists */
	@Test
	public void testPostWithInvalidWishlistOwner() {
		Wishlist wishlist = new Wishlist();
		wishlist.setId(UUID.randomUUID().toString());
		wishlist.setOwner("Test");
		instanceList.add(wishlist.getId());

		final Response response = createWishlist(wishlist);

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals(
				"Should return bad request when wishlist owner does not exist",
				Status.BAD_REQUEST.getStatusCode(), response.getStatus());
	}

	/* get() /wishlists/wishlistId */
	@Test
	public void testGetByWishlistId() {
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlist.getId());

		final Response response = target.request()
				.header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.get();

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("Response does not have expected response code",
				Status.OK.getStatusCode(), response.getStatus());
	}

	/* put(entity) /wishlists/wishlistId */
	@Test
	public void testPutByWishlistIdWithWishlist() {
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlist.getId());
		final Wishlist entityBody = wishlist;
		final Entity<Wishlist> entity = Entity.entity(entityBody,
				"application/json");

		final Response response = target.request()
				.header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.put(entity);

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("Response does not have expected response code",
				Status.OK.getStatusCode(), response.getStatus());
	}

	/* delete() /wishlists/wishlistId */
	@Test
	public void testDeleteByWishlistId() {
		final Response response = deleteWishlist(wishlist.getId());

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("Response does not have expected response code",
				Status.NO_CONTENT.getStatusCode(), response.getStatus());
	}

	/* post(null) /wishlists/wishlistId/media */
	@Test
	public void testPostByWishlistIdMedia() throws FileNotFoundException {
		Response response = createWishlistMedia();

		// Verify
		assertEquals(Response.Status.CREATED.getStatusCode(),
				response.getStatus());
		String location = response.getHeaderString("location").substring(
				response.getHeaderString("location").lastIndexOf("/") + 1);
		instanceListMedia.add(location);
		assertNotNull(location);
	}

	/* get() /wishlists/wishlistId/media */
	@Test
	public void testGetByWishlistIdMedia() throws MalformedURLException,
			NoSuchAlgorithmException, IOException {
		Response response = createWishlistMedia();
		String location = response.getHeaderString("location").substring(
				response.getHeaderString("location").lastIndexOf("/") + 1);
		instanceListMedia.add(location);

		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlist.getId() + "/media");
		final Response responseGet = target.request()
				.header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.get();

		Assert.assertNotNull("Response must not be null", responseGet);
		Assert.assertEquals("Response does not have expected response code",
				Status.OK.getStatusCode(), responseGet.getStatus());

		WishlistMedia[] wishlistMedias = responseGet
				.readEntity(WishlistMedia[].class);
		String actMD5 = null;
		for (WishlistMedia wishlistMedia : wishlistMedias) {
			if (location.equals(wishlistMedia.getId())) {
				actMD5 = computeMD5ChecksumForURL(new URL(wishlistMedia
						.getUri().toString()));
			}
		}

		String expMD5 = computeMD5ChecksumForFile(TEST_FILE_FOR_UPLOAD);
		Assert.assertEquals(
				"File on media repository is different from file sent", expMD5,
				actMD5);
	}

	/* delete() /wishlists/wishlistId/media/mediaId */
	@Test
	public void testDeleteByWishlistIdMediaByMediaId()
			throws FileNotFoundException {
		Response response = createWishlistMedia();
		String location = response.getHeaderString("location").substring(
				response.getHeaderString("location").lastIndexOf("/") + 1);

		final Response responseDelete = deleteWishlistMedia(wishlist.getId(),
				location);

		Assert.assertNotNull("Response must not be null", responseDelete);
		Assert.assertEquals("Response does not have expected response code",
				Status.NO_CONTENT.getStatusCode(), responseDelete.getStatus());
	}

	@After
	public void after() {
		for (String instance : instanceListMedia) {
			deleteWishlistMedia(wishlist.getId(), instance);
		}

		for (String instance : instanceList) {
			deleteWishlist(instance);
		}
	}

	private Response createWishlist(Wishlist wishlist) {
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path("");
		final Wishlist entityBody = wishlist;
		final Entity<Wishlist> entity = Entity.entity(entityBody,
				"application/json");

		return target.request().header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.post(entity);
	}

	private Response createWishlistMedia() throws FileNotFoundException {
		InputStream is = new FileInputStream(TEST_FILE_FOR_UPLOAD);

		URI requestUri = URI.create(REQUEST_URI + "/" + wishlist.getId()
				+ "/media");

		return cut.postByWishlistIdMedia(yaasAware, wishlist.getId(), is,
				requestUri);
	}

	private Response deleteWishlist(String wishlistId) {
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlistId);

		return target.request().header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.delete();
	}

	private Response deleteWishlistMedia(String wishlistId, String mediaId) {
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlistId + "/media/" + mediaId);

		return target.request().header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.delete();
	}

	@Override
	protected ResourceConfig configureApplication() {
		final ResourceConfig application = new ResourceConfig();
		application.register(DefaultWishlistsResource.class);
		return application;
	}

	public static String computeMD5ChecksumForFile(String filename)
			throws NoSuchAlgorithmException, IOException {
		InputStream inputStream = new FileInputStream(filename);
		return computeMD5ChecksumForInputStream(inputStream);
	}

	public static String computeMD5ChecksumForURL(URL input)
			throws MalformedURLException, IOException, NoSuchAlgorithmException {
		InputStream inputStream = input.openStream();
		return computeMD5ChecksumForInputStream(inputStream);
	}

	private static String computeMD5ChecksumForInputStream(
			InputStream inputStream) throws NoSuchAlgorithmException,
			IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");

		try {
			InputStream digestInputStream = new DigestInputStream(inputStream,
					md);
			while (digestInputStream.read() > 0) {
				;
			}
		} finally {
			inputStream.close();
		}
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < digest.length; i++) {
			sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return sb.toString();
	}
}
