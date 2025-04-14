package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import jakarta.ws.rs.core.Response.Status;

@Path("/changeState")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeStateResource {

  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
  private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response changeState(@HeaderParam("Authorization") String authToken, RegisterData data) {
    Key tokenKey = tokenKeyFactory.newKey(authToken);
    Entity tokenEntity = datastore.get(tokenKey);

    if (tokenEntity == null || System.currentTimeMillis() < tokenEntity.getLong("token_expiration")) {
      return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
    }

    String userRole = tokenEntity.getString("token_role");
    String usernameToChange = data.getUsername();
    String newState = data.getState();

    if (!newState.equals("ATIVADA") && !newState.equals("DESATIVADA") && !newState.equals("SUSPENSA")) {
      return Response.status(Status.BAD_REQUEST).entity("Invalid state. Allowed: ATIVADA, DESATIVADA, SUSPENSA.")
          .build();
    }

    Key userKey = userKeyFactory.newKey(usernameToChange);
    Entity user = datastore.get(userKey);
    if (user == null) {
      return Response.status(Status.NOT_FOUND).entity("User not found.").build();
    }

    String currentState = user.getString("user_account_status");

    if ("ADMIN".equals(userRole)) {
    } else if ("BACKOFFICE".equals(userRole) && "SUSPENSA".equals(currentState)) {
      return Response.status(Status.FORBIDDEN).entity("BACKOFFICE can only toggle between ATIVADA and DESATIVADA.")
          .build();
    } else {
      return Response.status(Status.FORBIDDEN).entity("You do not have permission to change states.").build();
    }

    Entity updatedUser = Entity.newBuilder(user)
        .set("user_account_status", newState)
        .build();

    datastore.put(updatedUser);

    return Response.ok("User state updated successfully.").build();
  }
}
