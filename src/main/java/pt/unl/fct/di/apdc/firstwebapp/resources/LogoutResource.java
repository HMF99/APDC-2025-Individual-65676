package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/logout")
public class LogoutResource {

  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response logout(@HeaderParam("Authorization") String authToken) {

    Key tokenKey = tokenKeyFactory.newKey(authToken);
    Entity tokenEntity = datastore.get(tokenKey);

    if (tokenEntity == null) {
      return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
    }

    datastore.delete(tokenKey);

    return Response.ok().entity("Logout successful.").build();
  }
}
