package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

import java.util.logging.Logger;

@Path("/changeAttributes")
public class ChangeAttributesResource {

    private static final Logger LOG = Logger.getLogger(ChangeAttributesResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeAccountAttributes(@HeaderParam("Authorization") String authToken,
            RegisterData newAccountData) {

        LOG.info("Attempting to change account attributes with token: " + authToken);

        Key tokenKey = tokenKeyFactory.newKey(authToken);
        Entity tokenEntity = datastore.get(tokenKey);

        if (tokenEntity == null || System.currentTimeMillis() < tokenEntity.getLong("token_expiration")) {
            return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        String requesterRole = tokenEntity.getString("token_role");
        String requesterUsername = tokenEntity.getString("token_username");

        if (requesterRole == null || requesterRole.isEmpty()) {
            LOG.severe("Missing requester role.");
            return Response.status(Status.BAD_REQUEST).entity("Role is required.").build();
        }

        if ("ENDUSER".equals(requesterRole) && !requesterUsername.equals(newAccountData.username)) {
            LOG.severe("ENDUSER role trying to modify another account.");
            return Response.status(Status.FORBIDDEN).entity("ENDUSER can only modify their own account.").build();
        }

        Entity userEntity = datastore.get(datastore.newKeyFactory().setKind("User").newKey(newAccountData.username));
        if (userEntity == null) {
            LOG.severe("User not found.");
            return Response.status(Status.NOT_FOUND).entity("User not found.").build();
        }

        String accountStatus = userEntity.contains("user_account_status") ? userEntity.getString("user_account_status")
                : "";
        if (!"ATIVADA".equalsIgnoreCase(accountStatus)) {
            LOG.severe("Account is not active.");
            return Response.status(Status.FORBIDDEN).entity("Account is not active. Modifications not allowed.")
                    .build();
        }

        if (newAccountData.email != null && (requesterRole.equals("ENDUSER") || requesterRole.equals("BACKOFFICE"))) {
            return Response.status(Status.BAD_REQUEST).entity("Email cannot be changed.").build();
        }
        if ("ENDUSER".equals(requesterRole)) {
            if (newAccountData.name != null || newAccountData.role != null || newAccountData.accountStatus != null) {
                LOG.severe("ENDUSER cannot change Username, Email, or Name.");
                return Response.status(Status.FORBIDDEN).entity("ENDUSER cannot change Name, Status or Role.")
                        .build();
            }
        } else if ("BACKOFFICE".equals(requesterRole)) {
            String targetRole = userEntity.getString("user_role");
            if (!"ENDUSER".equals(targetRole) && !"PARTNER".equals(targetRole)) {
                LOG.severe("BACKOFFICE can only modify ENDUSER or PARTNER accounts.");
                return Response.status(Status.FORBIDDEN)
                        .entity("BACKOFFICE can only modify ENDUSER or PARTNER accounts.").build();
            }
        }

        try {
            Entity.Builder userBuilder = Entity.newBuilder(userEntity);

            setIfPresent(userBuilder, "user_email", newAccountData.email, userEntity);
            setIfPresent(userBuilder, "user_name", newAccountData.name, userEntity);
            setIfPresent(userBuilder, "user_role", newAccountData.role, userEntity);
            setIfPresent(userBuilder, "user_account_status", newAccountData.accountStatus, userEntity);
            setIfPresent(userBuilder, "user_phone", newAccountData.phoneNumber, userEntity);
            setIfPresent(userBuilder, "user_profile", newAccountData.profile, userEntity);
            setIfPresent(userBuilder, "user_photo", newAccountData.photo, userEntity);
            setIfPresent(userBuilder, "user_address", newAccountData.address, userEntity);
            setIfPresent(userBuilder, "user_citizenCardNumber", newAccountData.citizenCardNumber, userEntity);
            setIfPresent(userBuilder, "user_employer", newAccountData.employer, userEntity);
            setIfPresent(userBuilder, "user_employerNif", newAccountData.employerNif, userEntity);
            setIfPresent(userBuilder, "user_job", newAccountData.job, userEntity);
            setIfPresent(userBuilder, "user_nif", newAccountData.nif, userEntity);

            Entity updatedUserEntity = userBuilder.build();
            datastore.put(updatedUserEntity);

            LOG.info("Account updated successfully for user: " + newAccountData.username);
            return Response.status(Status.OK).entity("Account updated successfully.").build();

        } catch (DatastoreException e) {
            LOG.severe("Datastore error: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error updating account in Datastore.").build();
        } catch (Exception e) {
            LOG.severe("General error: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error updating account.").build();
        }
    }

    private void setIfPresent(Entity.Builder builder, String propertyName, String newValue, Entity oldEntity) {
        if (newValue != null && !newValue.isEmpty()) {
            builder.set(propertyName, newValue);
        } else if (oldEntity.contains(propertyName)) {
            builder.set(propertyName, oldEntity.getString(propertyName));
        } else {
            builder.set(propertyName, "NOT DEFINED");
        }
    }
}
