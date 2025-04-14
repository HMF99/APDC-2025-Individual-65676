package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import jakarta.ws.rs.core.Response.Status;

@Path("/changeRole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeRoleResource {

  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
  private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response changeRole(@HeaderParam("Authorization") String authToken, RegisterData data) {
    Key tokenKey = tokenKeyFactory.newKey(authToken);
    Entity tokenEntity = datastore.get(tokenKey);

    if (tokenEntity == null || System.currentTimeMillis() < tokenEntity.getLong("token_expiration")) {
      return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
    }

    String usernameToChange = data.getUsername();
    String newRole = data.getRole();
    String userRole = tokenEntity.getString("token_role");

    if (!newRole.equals("ENDUSER") && !newRole.equals("PARTNER") && !newRole.equals("BACKOFFICE")
        && !newRole.equals("ADMIN")) {
      return Response.status(Status.BAD_REQUEST).entity("Invalid state. Allowed: ATIVADA, DESATIVADA, SUSPENSA.")
          .build();
    }

    Key userKey = userKeyFactory.newKey(usernameToChange);
    Entity user = datastore.get(userKey);
    if (user == null) {
      return Response.status(Status.NOT_FOUND).entity("User not found.").build();
    }

    if ("ENDUSER".equals(userRole) || "PARTNER".equals(userRole)) {
      return Response.status(Status.FORBIDDEN).entity("You do not have permission to change roles.").build();
    }

    if ("BACKOFFICE".equals(userRole)) {
      if (!("ENDUSER".equals(newRole) || "PARTNER".equals(newRole))) {
        return Response.status(Status.FORBIDDEN).entity("BACKOFFICE can only change roles between ENDUSER and PARTNER.")
            .build();
      }
    }

    Entity updatedUser = Entity.newBuilder(user)
        .set("user_role", newRole)
        .build();

    datastore.put(updatedUser);

    return Response.ok("User role updated successfully.").build();
  }
}
