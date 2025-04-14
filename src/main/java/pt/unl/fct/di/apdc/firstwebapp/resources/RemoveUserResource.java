
package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import jakarta.ws.rs.core.Response.Status;

@Path("/removeUser")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveUserResource {

  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
  private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response removeUser(@HeaderParam("Authorization") String authToken, RegisterData data) {
    // Validar o token
    Key tokenKey = tokenKeyFactory.newKey(authToken);
    Entity tokenEntity = datastore.get(tokenKey);

    if (tokenEntity == null || System.currentTimeMillis() < tokenEntity.getLong("token_expiration")) {
      return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
    }

    String userRole = tokenEntity.getString("token_role");

    if (!("ADMIN".equals(userRole) || "BACKOFFICE".equals(userRole))) {
      return Response.status(Status.FORBIDDEN).entity("You do not have permission to remove accounts.").build();
    }

    String usernameToDelete = data.getUsername();
    String emailToDelete = data.getEmail();

    Entity user = null;

    if (usernameToDelete != null && !usernameToDelete.isEmpty()) {
      Key userKey = userKeyFactory.newKey(usernameToDelete);
      user = datastore.get(userKey);
      if (user == null) {
        return Response.status(Status.NOT_FOUND).entity("User not found with username.").build();
      }
      datastore.delete(userKey);
    } else if (emailToDelete != null && !emailToDelete.isEmpty()) {
      Query<Entity> query = Query.newEntityQueryBuilder()
          .setKind("User")
          .setFilter(StructuredQuery.PropertyFilter.eq("user_email", emailToDelete))
          .build();

      QueryResults<Entity> results = datastore.run(query);

      if (!results.hasNext()) {
        return Response.status(Status.NOT_FOUND).entity("User not found with email.").build();
      }

      user = results.next();
      Key userKey = user.getKey();
      datastore.delete(userKey);
    } else {
      return Response.status(Status.BAD_REQUEST).entity("Username or email must be provided.").build();
    }

    return Response.ok("User removed successfully.").build();
  }
}
