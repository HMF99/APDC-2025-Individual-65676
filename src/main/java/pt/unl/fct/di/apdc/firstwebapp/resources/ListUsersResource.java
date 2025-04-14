package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;
import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/listUsers")
public class ListUsersResource {

  private static final Logger LOG = Logger.getLogger(ListUsersResource.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response listUsers(@HeaderParam("Authorization") String authToken) {

    Key tokenKey = tokenKeyFactory.newKey(authToken);
    Entity tokenEntity = datastore.get(tokenKey);

    if (tokenEntity == null || System.currentTimeMillis() < tokenEntity.getLong("token_expiration")) {
      return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
    }

    String requesterRole = tokenEntity.getString("token_role");

    if (requesterRole == null || requesterRole.isEmpty()) {
      LOG.warning("Missing requester role.");
      return Response.status(Status.BAD_REQUEST).entity("Role is required.").build();
    }

    Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();
    QueryResults<Entity> results = datastore.run(query);

    List<UserData> rUsersList = new ArrayList<>();
    List<RegisterData> usersList = new ArrayList<>();

    LOG.info("Query executed. Now processing results...");

    while (results.hasNext()) {
      Entity userEntity = results.next();
      LOG.fine("Processing user: " + userEntity.getKey().getName());

      String role = userEntity.contains("user_role") ? userEntity.getString("user_role") : "";
      String profile = userEntity.contains("user_profile") ? userEntity.getString("user_profile") : "";
      String accountStatus = userEntity.contains("user_account_status") ? userEntity.getString("user_account_status")
          : "";

      if ("ENDUSER".equals(requesterRole)) {
        LOG.fine("Checking if user is valid for ENDUSER role...");
        if (!"ENDUSER".equals(role))
          continue;
        if (!"public".equalsIgnoreCase(profile))
          continue;
        if (!"ATIVADA".equalsIgnoreCase(accountStatus))
          continue;
      }

      if ("ENDUSER".equals(requesterRole)) {
        UserData user = new UserData();

        user.username = userEntity.getKey().getName();
        user.email = getOrDefault(userEntity, "user_email");
        user.name = getOrDefault(userEntity, "user_name");
        rUsersList.add(user);
      } else {

        RegisterData user = new RegisterData();
        user.username = userEntity.getKey().getName();
        user.email = getOrDefault(userEntity, "user_email");
        user.name = getOrDefault(userEntity, "user_name");
        user.password = getOrDefault(userEntity, "user_pwd");
        user.confirmation = getOrDefault(userEntity, "user_pwd");
        user.profile = getOrDefault(userEntity, "user_profile");
        user.accountStatus = getOrDefault(userEntity, "user_account_status");
        user.role = getOrDefault(userEntity, "user_role");
        user.photo = getOrDefault(userEntity, "user_photo");
        user.address = getOrDefault(userEntity, "user_address");
        user.citizenCardNumber = getOrDefault(userEntity, "user_citizenCardNumber");
        user.employer = getOrDefault(userEntity, "user_employer");
        user.employerNif = getOrDefault(userEntity, "user_employerNif");
        user.job = getOrDefault(userEntity, "user_job");
        user.nif = getOrDefault(userEntity, "user_nif");
        user.phoneNumber = getOrDefault(userEntity, "user_phoneNumber");
        usersList.add(user);
      }

    }
    if (requesterRole.equals("ENDUSER")) {
      if (rUsersList.isEmpty()) {
        LOG.info("No users to list, either due to filters or empty database.");
        return Response.status(Status.NO_CONTENT).entity("No users found.").build();
      }
      LOG.info("Returning list of users. Total count: " + rUsersList.size());
      return Response.ok(rUsersList).build();
    } else {
      if (usersList.isEmpty()) {
        LOG.info("No users to list, either due to filters or empty database.");
        return Response.status(Status.NO_CONTENT).entity("No users found.").build();
      }

      LOG.info("Returning list of users. Total count: " + usersList.size());
      return Response.ok(usersList).build();
    }
  }

  // presente
  private String getOrDefault(Entity entity, String property) {
    return entity.contains(property) && !entity.getString(property).isEmpty()
        ? entity.getString(property)
        : "NOT DEFINED";
  }

}