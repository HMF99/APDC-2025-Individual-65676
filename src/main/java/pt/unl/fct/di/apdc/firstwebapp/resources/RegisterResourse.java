package pt.unl.fct.di.apdc.firstwebapp.resources;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreException;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.cloud.datastore.Transaction;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/register")
public class RegisterResourse {

  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

  public RegisterResourse() {
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response registerUser(RegisterData data) {

    if (data.role == null || data.role.isEmpty()) {
      data.role = "ENDUSER";
    }
    if (data.accountStatus == null || data.accountStatus.isEmpty()) {
      data.accountStatus = "DESATIVADA";
    }

    if (!data.validRegistration()) {
      return Response.status(Status.BAD_REQUEST).entity("Missing or invalid fields.").build();
    }

    if (data.email == null || data.email.trim().isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Email is required.").build();
    }

    if (!data.password.equals(data.confirmation)) {
      return Response.status(Status.BAD_REQUEST).entity("Password and confirmation do not match.").build();
    }

    Transaction txn = datastore.newTransaction();

    try {
      Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
      Entity existingUser = txn.get(userKey);

      if (existingUser != null) {
        txn.rollback();
        return Response.status(Status.CONFLICT).entity("User already exists.").build();
      }

      Query<Entity> emailQuery = Query.newEntityQueryBuilder()
          .setKind("User")
          .setFilter(StructuredQuery.PropertyFilter.eq("user_email", data.email.toLowerCase()))
          .build();

      QueryResults<Entity> results = txn.run(emailQuery);

      if (results.hasNext()) {
        txn.rollback();
        return Response.status(Status.CONFLICT).entity("Email already exists.").build();
      }

      Entity.Builder userBuilder = Entity.newBuilder(userKey)
          .set("user_name", data.name)
          .set("user_email", data.email.toLowerCase())
          .set("user_pwd", DigestUtils.sha512Hex(data.password))
          .set("user_phone", data.phoneNumber)
          .set("user_profile", data.profile.toLowerCase())
          .set("user_creation_time", Timestamp.now());

      if (data.citizenCardNumber != null && !data.citizenCardNumber.isEmpty()) {
        userBuilder.set("user_citizen_card_number", data.citizenCardNumber);
      }

      if (data.role != null && !data.role.isEmpty()) {
        userBuilder.set("user_role", data.role);
      }

      if (data.nif != null && !data.nif.isEmpty()) {
        userBuilder.set("user_nif", data.nif);
      }

      if (data.employer != null && !data.employer.isEmpty()) {
        userBuilder.set("user_employer_entity", data.employer);
      }

      if (data.job != null && !data.job.isEmpty()) {
        userBuilder.set("user_function", data.job);
      }

      if (data.address != null && !data.address.isEmpty()) {
        userBuilder.set("user_address", data.address);
      }

      if (data.employerNif != null && !data.employerNif.isEmpty()) {
        userBuilder.set("user_employer_nif", data.employerNif);
      }

      if (data.accountStatus != null && !data.accountStatus.isEmpty()) {
        userBuilder.set("user_account_status", data.accountStatus);
      }

      if (data.photo != null && !data.photo.isEmpty()) {
        userBuilder.set("user_photo", data.photo);
      }

      Entity userEntity = userBuilder.build();

      txn.put(userEntity);
      txn.commit();

      return Response.ok().entity("User successfully registered.").build();

    } catch (DatastoreException e) {
      if (txn.isActive()) {
        txn.rollback();
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal error.").build();
    }
  }
}
