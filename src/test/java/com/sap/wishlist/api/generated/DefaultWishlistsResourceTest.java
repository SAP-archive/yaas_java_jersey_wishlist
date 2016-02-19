package com.sap.wishlist.api.generated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
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
	private static final String WISHLIST_ITEMS_PATH = "wishlistItems";
	private static final String CLIENT = "test";
	private static final String TEST_FILE_FOR_UPLOAD = "src/test/resources/testMedia.png";

	private static Wishlist wishlist;

	private ArrayList<String> instanceList;
	private ArrayList<String> instanceListMedia;

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

		instanceList = new ArrayList<String>();
		instanceListMedia = new ArrayList<String>();
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
		final Wishlist otherWishlist = new Wishlist();
		otherWishlist.setId(UUID.randomUUID().toString());
		otherWishlist.setOwner(TestConstants.CUSTOMER);
		instanceList.add(otherWishlist.getId());

		final Response response = createWishlist(otherWishlist);

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("Response does not have expected response code",
				Status.CREATED.getStatusCode(), response.getStatus());
	}

	/* post(entity) /wishlists */
	@Test
	public void testPostCheckDuplicateID() {
		final Wishlist otherWishlist = new Wishlist();
		otherWishlist.setId(UUID.randomUUID().toString());
		otherWishlist.setOwner(TestConstants.CUSTOMER);
		instanceList.add(otherWishlist.getId());
		createWishlist(otherWishlist);

		final Response response = createWishlist(otherWishlist);

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals(
				"Should return conflict when wishlist id is already used",
				Status.CONFLICT.getStatusCode(), response.getStatus());
	}

	/* post(entity) /wishlists */
	@Test
	public void testPostWithInvalidWishlistOwner() {
		final Wishlist otherWishlist = new Wishlist();
		otherWishlist.setId(UUID.randomUUID().toString());
		otherWishlist.setOwner("Test");
		instanceList.add(otherWishlist.getId());

		final Response response = createWishlist(otherWishlist);

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
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlist.getId() + "/media");

		final Response response = target
				.request()
				.header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.post(Entity.entity(new ByteArrayInputStream("test".getBytes()), MediaType.APPLICATION_OCTET_STREAM_TYPE));

		// Verify
		assertEquals(Response.Status.CREATED.getStatusCode(),
				response.getStatus());
		final String mediaId = response.getHeaderString("location").substring(
				response.getHeaderString("location").lastIndexOf("/") + 1);
		instanceListMedia.add(mediaId);
		assertNotNull(mediaId);
	}

	/* get() /wishlists/wishlistId/media */
	@Test
	public void testGetByWishlistIdMedia() throws MalformedURLException,
			NoSuchAlgorithmException, IOException {
		final String mediaId = createWishlistMediaInternal();
		instanceListMedia.add(mediaId);

		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlist.getId() + "/media");
		final Response responseGet = target.request()
				.header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.get();

		Assert.assertNotNull("Response must not be null", responseGet);
		Assert.assertEquals("Response does not have expected response code",
				Status.OK.getStatusCode(), responseGet.getStatus());

		final WishlistMedia[] wishlistMedias = responseGet
				.readEntity(WishlistMedia[].class);
		String actMD5 = null;
		for (final WishlistMedia wishlistMedia : wishlistMedias) {
			if (mediaId.equals(wishlistMedia.getId())) {
				actMD5 = computeMD5ChecksumForURL(new URL(wishlistMedia
						.getUri().toString()));
			}
		}

		final String expMD5 = computeMD5ChecksumForFile(TEST_FILE_FOR_UPLOAD);
		Assert.assertEquals(
				"File on media repository is different from file sent", expMD5,
				actMD5);
	}

	/* delete() /wishlists/wishlistId/media/mediaId */
	@Test
	public void testDeleteByWishlistIdMediaByMediaId()
			throws FileNotFoundException {
		final String mediaId = createWishlistMediaInternal();

		final Response responseDelete = deleteWishlistMedia(wishlist.getId(),
				mediaId);

		Assert.assertNotNull("Response must not be null", responseDelete);
		Assert.assertEquals("Response does not have expected response code",
				Status.NO_CONTENT.getStatusCode(), responseDelete.getStatus());
	}

	@After
	public void after() {
		for (final String instance : instanceListMedia) {
			deleteWishlistMedia(wishlist.getId(), instance);
		}

		for (final String instance : instanceList) {
			deleteWishlist(instance);
		}
	}

	private Response createWishlist(final Wishlist wishlistToCreate) {
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path("");
		final Wishlist entityBody = wishlistToCreate;
		final Entity<Wishlist> entity = Entity.entity(entityBody,
				"application/json");

		return target.request().header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.post(entity);
	}

	private String createWishlistMediaInternal() throws FileNotFoundException {
		final InputStream is = new FileInputStream(TEST_FILE_FOR_UPLOAD);

		return cut.createWishlistMedia(yaasAware, wishlist.getId(), is);
	}

	private Response deleteWishlist(final String wishlistId) {
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlistId);

		return target.request().header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.delete();
	}

	private Response deleteWishlistMedia(final String wishlistId, final String mediaId) {
		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlistId + "/media/" + mediaId);

		return target.request().header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.delete();
	}

	@Test
	// get() /wishlists/wishlistId/wishlistItems
	public void testGetByWishlistIdWishlistItems() {

		final WebTarget target = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlist.getId() + "/" + WISHLIST_ITEMS_PATH);
		final Response response = target.request()
				.header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.get();

		Assert.assertNotNull("Response must not be null", response);
		Assert.assertEquals("Response does not have expected response code",
				Status.OK.getStatusCode(), response.getStatus());
		Assert.assertNotNull("Response must not be null", response.readEntity(WishlistItem[].class));
	}

	@Test
	// post() /wishlists/wishlistId/wishlistItems
	public void testPostByWishlistIdWishlistItems() {
		final List<WishlistItem> wishlistItems = new ArrayList<WishlistItem>();
		final WishlistItem item = new WishlistItem();
		item.setProduct("Item1");
		item.setAmount(1);
		wishlistItems.add(item);
		wishlist.setItems(wishlistItems);

		final WebTarget targetPost = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlist.getId() + "/" + WISHLIST_ITEMS_PATH);

		final Entity<WishlistItem> entity = Entity.entity(item,
				"application/json");

		final Response responsePost = targetPost.request()
				.header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.post(entity);

		Assert.assertNotNull("Response must not be null", responsePost);
		Assert.assertEquals("Response does not have expected response code",
				Status.CREATED.getStatusCode(), responsePost.getStatus());

		final WebTarget targetGet = getRootTarget(ROOT_RESOURCE_PATH).path(
				"/" + wishlist.getId() + "/" + WISHLIST_ITEMS_PATH);
		final Response responseGet = targetGet.request()
				.header(YaasAwareTrait.Headers.CLIENT, CLIENT)
				.header(YaasAwareTrait.Headers.TENANT, TestConstants.TENANT)
				.get();

		Assert.assertNotNull("Response must not be null", responseGet);
		Assert.assertEquals("Response does not have expected response code",
				Status.OK.getStatusCode(), responseGet.getStatus());

		Assert.assertEquals(1, responseGet.readEntity(WishlistItem[].class).length);
	}


	@Override
	protected ResourceConfig configureApplication() {
		final ResourceConfig application = new ResourceConfig();
		application.register(DefaultWishlistsResource.class);
		return application;
	}

	public static String computeMD5ChecksumForFile(final String filename)
			throws NoSuchAlgorithmException, IOException {
		final InputStream inputStream = new FileInputStream(filename);
		return computeMD5ChecksumForInputStream(inputStream);
	}

	public static String computeMD5ChecksumForURL(final URL input)
			throws MalformedURLException, IOException, NoSuchAlgorithmException {
		final InputStream inputStream = input.openStream();
		return computeMD5ChecksumForInputStream(inputStream);
	}

	private static String computeMD5ChecksumForInputStream(
			final InputStream inputStream) throws NoSuchAlgorithmException,
					IOException {
		final MessageDigest md = MessageDigest.getInstance("MD5");

		try {
			final InputStream digestInputStream = new DigestInputStream(inputStream,
					md);
			while (digestInputStream.read() > 0) {
				// do nothing
			}
		}
		finally {
			inputStream.close();
		}
		final byte[] digest = md.digest();
		final StringBuffer sb = new StringBuffer();

		for (final byte element : digest) {
			sb.append(Integer.toString((element & 0xff) + 0x100, 16)
					.substring(1));
		}
		return sb.toString();
	}
}
