package pt.unl.fct.di.apdc.firstwebapp.resources;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.apdc.firstwebapp.util.WorkSheetData;

@Path("/createWorkSheet")
public class CreateWorkSheetResource {

  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createWorkSheet(@HeaderParam("Authorization") String authToken, WorkSheetData data) {

    Key tokenKey = tokenKeyFactory.newKey(authToken);
    Entity tokenEntity = datastore.get(tokenKey);

    if (tokenEntity == null || System.currentTimeMillis() < tokenEntity.getLong("token_expiration")) {
      return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
    }

    String username = tokenEntity.getString("token_username");
    Key userKey = datastore.newKeyFactory().setKind("User").newKey(username);
    Entity userEntity = datastore.get(userKey);

    if (userEntity == null || !userEntity.getString("user_role").equals("BACKOFFICE")) {
      return Response.status(Response.Status.FORBIDDEN).entity("Insufficient permissions").build();
    }

    if (data.reference == null || data.reference.isBlank() ||
        data.description == null || data.description.isBlank() ||
        data.targetType == null || data.targetType.isBlank() ||
        data.adjudicationState == null || data.adjudicationState.isBlank()) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Missing required fields").build();
    }

    boolean isAdjudicated = data.adjudicationState.equals("ADJUDICADO");

    if (isAdjudicated) {
      if (data.adjudicationDate == null ||
          data.expectedStartDate == null ||
          data.expectedEndDate == null ||
          data.partnerUsername == null || data.partnerUsername.isBlank() ||
          data.companyName == null || data.companyName.isBlank() ||
          data.companyNif == null || data.companyNif.isBlank() ||
          data.workStatus == null || data.workStatus.isBlank()) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Missing adjudication fields").build();
      }
    }

    Key workSheetKey = datastore.newKeyFactory().setKind("WorkSheet").newKey(data.reference);

    Entity.Builder builder = Entity.newBuilder(workSheetKey)
        .set("reference", data.reference)
        .set("description", data.description)
        .set("targetType", data.targetType)
        .set("adjudicationState", data.adjudicationState);

    if (isAdjudicated) {
      builder.set("adjudicationDate", data.adjudicationDate)
          .set("expectedStartDate", data.expectedStartDate)
          .set("expectedEndDate", data.expectedEndDate)
          .set("partnerUsername", data.partnerUsername)
          .set("partnerName", data.companyName)
          .set("partnerNIF", data.companyNif)
          .set("status", data.workStatus)
          .set("notes", data.notes == null ? "" : data.notes);
    }

    datastore.put(builder.build());

    return Response.status(Response.Status.OK).entity("WorkSheet created successfully").build();
  }
}
