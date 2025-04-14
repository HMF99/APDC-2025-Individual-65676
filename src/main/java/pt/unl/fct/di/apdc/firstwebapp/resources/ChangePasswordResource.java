package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.PasswordData;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.logging.Logger;

@Path("/changePassword")
public class ChangePasswordResource {

  private static final Logger LOG = Logger.getLogger(ChangePasswordResource.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");
  private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response changePassword(@HeaderParam("Authorization") String authToken, PasswordData data) {

    Key tokenKey = tokenKeyFactory.newKey(authToken);
    Entity tokenEntity = datastore.get(tokenKey);

    if (tokenEntity == null) {
      return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
    }

    String tokenUsername = tokenEntity.getString("token_username");

    Key userKey = userKeyFactory.newKey(tokenUsername);
    Entity user = datastore.get(userKey);
    if (user == null) {
      return Response.status(Response.Status.NOT_FOUND).entity("User not found.").build();
    }

    String storedPasswordHash = user.getString("user_pwd");
    String inputPasswordHash = DigestUtils.sha512Hex(data.password);

    if (!storedPasswordHash.equals(inputPasswordHash)) {
      return Response.status(Response.Status.FORBIDDEN).entity("Incorrect current password.").build();
    }

    if (!data.newPassword.equals(data.confirmation)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("New passwords do not match.").build();
    }

    if (data.newPassword.equals(storedPasswordHash)) {
      return Response.status(Response.Status.BAD_REQUEST)
          .entity("New password must be different from current password.").build();
    }

    String newPasswordHash = DigestUtils.sha512Hex(data.newPassword);

    Entity updatedUser = Entity.newBuilder(user)
        .set("user_pwd", newPasswordHash)
        .build();

    datastore.put(updatedUser);

    LOG.info("Password updated successfully for user: " + tokenUsername);
    return Response.status(Response.Status.OK).entity("Password changed successfully.").build();
  }
}