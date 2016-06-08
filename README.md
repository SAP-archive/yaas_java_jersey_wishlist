# yaas_java_jersey_wishlist
This is an example implementation of the YaaS [Wishlist service](https://devportal.yaas.io/gettingstarted/createaservice/index.html) based on Java. It shows how to implement a basic service and how to integrate with other services on YaaS.


License
-------

This project is licensed under the Apache Software License, v. 2 except as noted otherwise in the LICENSE file.


API Console
-----------

You can open the API Console in a separate window by using the following link:
- [API Console](https://api.yaas.io/hybris/java-jersey-wishlist/example)



API Overview
------------

This service provides REST endpoints for interacting with several YaaS services.

### Document Service
The endpoint `/wishlists` enables you to:
- Interact with wishlists in a CRUD fashion.
  - Get a list of all wishlists within a tenant.
  - Create a new wishlist.
  - Get a specific wishlist based on an ID.
  - Update a specific wishlist based on an ID.
  - Delete a specific wishlist based on an ID.

The endpoint `/wishlists/{wishlistId}/wishlistItems` enables you to:
  - Get a specific wishlist and read its items.
  - Create a new wishlist item and add it to the specific wishlist.
  
See also [WishlistService.java](src/main/java/com/sap/wishlist/service/WishlistService.java).

### Email Service
An email is sent to the wishlist owner when a wishlist is created. For more details, see the `sendMail` method in [WishlistService.java](src/main/java/com/sap/wishlist/service/WishlistService.java).

### Media Service
The endpoint `/wishlists/{wishlistId}/media` enables you to:
  - Get a list of all media for the wishlist.
  - Create a new media file.
  - Delete a specific media file based on an ID.

See also [WishlistMediaService.java](src/main/java/com/sap/wishlist/service/WishlistMediaService.java)

### Customer Service
When a wishlist is being created, the implementation checks if its owner exists as a customer. 
You can find the details at the beginning of the **POST** method in [WishlistService.java](src/main/java/com/sap/wishlist/service/WishlistService.java).


Purpose and Benefits
------------------

Showcase how a service can be written using Java. Demonstrate the integration with other services in YaaS, including authentication. Topics covered:
- Usage of Spring framework
- Property handling
- Authentication with the YaaS platform
- Consumption of YaaS services
- Deployment to Cloud Foundry
- Testing


Dependencies
------------

- Core Services
  - [OAuth2](https://devportal.yaas.io/services/oauth2/latest/index.html)
  - [Document](https://devportal.yaas.io/services/document/latest/index.html)
  - [Email](https://devportal.yaas.io/services/email/latest/index.html)
  - [Media](https://devportal.yaas.io/services/media/latest/index.html)
  - [Customer](https://devportal.yaas.io/services/customer/latest/index.html)


Build, Test, and Run the Service
-------------------------------

See the [Setup Guide](SETUP.md) for instructions on how to configure this service.

1. Use `mvn clean install` to build the service and run the tests.

2. Run the service locally by calling `mvn jetty:run`, and navigate to the local [API Console](http://localhost:8080).


FAQ / Troubleshooting
---------------------

If you get failed tests while building with `mvn clean install`, such as `response code expected:201 but was:500`, then it might mean that the service can't connect to other YaaS services. This could be related to missing proxy settings.

Hint: In that case, you might want to try it out with:

    mvn clean install -Dhttp.proxyPort=<proxyPort> -Dhttp.proxyHost=<proxyHost> -Dhttps.proxyPort=<proxyPort> -Dhttps.proxyHost=<proxyHost>
