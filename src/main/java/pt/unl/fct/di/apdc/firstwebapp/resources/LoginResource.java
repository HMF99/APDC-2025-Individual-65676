package pt.unl.fct.di.apdc.firstwebapp.resources;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.gson.Gson;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password.";
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final KeyFactory tokenKeyFactory = datastore.newKeyFactory().setKind("Token");

	private final Gson g = new Gson();

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogin(LoginData data) {

		try {
			Key userKey = userKeyFactory.newKey(data.username);
			Entity user = datastore.get(userKey);

			if (user == null) {
				return Response.status(Response.Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS).build();
			}

			String hashedPWD = user.getString("user_pwd");
			String inputPasswordHash = DigestUtils.sha512Hex(data.password);

			if (hashedPWD.equals(inputPasswordHash)) {
				String role = user.getString("user_role");
				AuthToken token = new AuthToken(data.username, role);

				Key tokenKey = tokenKeyFactory.newKey(token.tokenID);
				Entity tokenEntity = Entity.newBuilder(tokenKey)
						.set("token_username", token.username)
						.set("token_role", token.role)
						.set("token_creation", token.creationDate)
						.set("token_expiration", token.expirationDate)
						.set("token_verifier", token.verifier)
						.build();
				datastore.put(tokenEntity);

				return Response.ok(g.toJson(token)).build();
			} else {
				return Response.status(Response.Status.FORBIDDEN).entity(MESSAGE_INVALID_CREDENTIALS).build();
			}
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error.").build();
		}
	}
}
