package com.sap.wishlist.utility;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

public class ErrorHandler {

	public static void handleResponse(Response response) {
		switch (response.getStatus()) {
		case 400:
			throw new BadRequestException();
		case 401:
			throw new NotAuthorizedException(response);
		case 403:
			throw new ForbiddenException();
		case 404:
			throw new NotFoundException();
		}
	}
}