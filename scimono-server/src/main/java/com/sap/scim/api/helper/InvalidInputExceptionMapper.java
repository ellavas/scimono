
package com.sap.scim.api.helper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.sap.scim.entity.ErrorResponse;
import com.sap.scim.exception.InvalidInputException;

public class InvalidInputExceptionMapper implements ExceptionMapper<InvalidInputException> {

  @Override
  public Response toResponse(final InvalidInputException exception) {
    ErrorResponse scimError = new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), null, exception.getMessage());

    return Response.status(Response.Status.BAD_REQUEST).entity(scimError).build();
  }

}
