This file provides you background information for setting up and running the sample implementation of the Wishlist service.


Prerequisites
-------------

To get the most of this guide, you should be able to work with [Java](http://www.java.com/), [Apache Maven](http://maven.apache.org/), and [git](https://git-scm.com/).

The list of required software and operating systems is listed on the [Prerequisites](https://devportal.yaas.io/gettingstarted/prerequisites/index.html) page of the Getting Started guides on the Dev Portal.


Set Up a Project and Create an Client
------------------------------------------

You need credentials to access APIs on YaaS. The easiest way to get such credentials is to declare that the API calls are made by one of your applications.

1. Register and sign in to the [Builder](https://builder.yaas.io/).

2. Create or join an organization.

   If you don't have an organization yet, we suggest you create an organization with a non-commercial purpose. This will allow you to carry on with the subscription to beta packages.

3. Follow the instructions of the [Create Projects](https://devportal.yaas.io/tools/builder/#CreateProjects) guide to create a new project and subscribe to the following beta packages:

    - Customer Accounts (Beta)
    - Email (Beta)
    - Media (Beta)
    - Persistence (Beta)

    Take note of the **Identifier** of your project.

4. Create an client for your project and select the required scopes:

    - **hybris.customer_read**
    - **hybris.customer_create**
    - **hybris.email_manage**
    - **hybris.email_send**
    - **hybris.media_manage**
    - **hybris.document_view**
    - **hybris.document_manage**

    Take note of the **Identifier** of your client, as well as of its **Client ID** and **Client Secret**.


Create a Customer
-----------------

From the point of view of this service, a wishlist belongs to one of your customers. This means that you need to [create a test customer account](https://devportal.yaas.io/services/customer/latest/index.html#CreateNewAccount) to carry on.

1. Get a token to authenticate and authorize the API call:

    1. Open the [API console for the OAuth2 API](https://devportal.yaas.io/services/oauth2/latest/apiconsole.html) on the Dev Portal.
    2. Select the **POST** method for the endpoint `/token`, then click **Try It**.
    3. Enter the **client_id** and **client_secret** of your client.
    4. Enter the **grant_type** : `client_credentials`.
    5. Enter the **scope**: `hybris.customer_create`.
    6. Send the request by clicking **POST**. Check that the OAuth2 service returns a response with the status `200 (OK)` and with a body containing the requested scope.
    7. Locate the **access_token** in the body of the response and take note of its value.

2. Create the customer:

    1. Open the [API console for the Customer API](https://devportal.yaas.io/services/customer/latest/apiconsole.html) on the Dev Portal.
    2. Select the **POST** method for the endpoint `{tenant}/customers`, then click **Try It**.
    3. Enter the **tenant** URL parameter: Use the **Identifier** of your project.
    4. Enter the **Authorization** header: `Bearer `, followed by the access token received above.
    5. Enter the **Body**:

            {
                "firstName": "<your first name>",
                "contactEmail": "<your e-mail address>"
            }

    6. Send the request by clicking **POST**. Check that the Customer service returns a response with the status `201 (Created)` and with a body containing the customer information.
    7. Take note of the **id** of your new customer.


Download and Customize the Source Code
--------------------------------------

The source code contains automated tests that ensure that the service works as expected. These tests need information about your test project. Moreover, you need to provide your client credentials so that the service can authenticate against YaaS.

1. Clone the git repository:

        git clone https://github.com/SAP/yaas_java_jersey_wishlist.git
        cd yaas_java_jersey_wishlist

2. Use your favorite editor to edit the file [TestConstants.java](src/test/java/com/sap/wishlist/api/TestConstants.java) under `src/test/java/com/sap/wishlist/api`:

    * Update the constant **TENANT**: Set it to the **Identifier** of your project.
    * Update the constant **CUSTOMER**: Set it to the **id** of your customer.

3. Edit the file [default.properties](src/main/resources/default.properties) under `src/main/resources`:

    * Set the parameter **YAAS_CLIENT_IS_APPLICATION** to `true`, as you are using the credentials of your client and run in single tenant mode.
    * Set the parameter **YAAS_CLIENT** to the **Identifier** of your client
    * Set the parameter **YAAS_CLIENT_ID** to the **Client ID** of your client.
    * Set the parameter **YAAS_CLIENT_SECRET** to the **Client Secret** of your client.

It is recommended that you provide these credentials using environment variables. When deploying the service to CloudFoundry, provide the credentials as environment entries within the manifest.yml used for the deployment.

However, in this example, we set them in the source code for the sake of simplicity.
