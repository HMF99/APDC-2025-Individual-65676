package pt.unl.fct.di.apdc.firstwebapp.util;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import java.util.logging.Logger;
import org.apache.commons.codec.digest.DigestUtils;

public class AppInit {

  private static final Logger LOG = Logger.getLogger(AppInit.class.getName());
  private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
  private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

  public static void initRootUser() {
    Query<Entity> adminQuery = Query.newEntityQueryBuilder()
        .setKind("User")
        .setFilter(StructuredQuery.PropertyFilter.eq("user_role", "ADMIN"))
        .setLimit(1)
        .build();

    QueryResults<Entity> admins = datastore.run(adminQuery);

    if (admins.hasNext()) {
      LOG.severe("At least one ADMIN user already exists. Skipping root creation.");
      Entity admin = admins.next();
      LOG.info("Existing admin user found: " + admin.getString("user_name"));
      return;
    }

    String rootUsername = "hFeteira";
    Key rootKey = userKeyFactory.newKey(rootUsername);

    Entity rootUser = Entity.newBuilder(rootKey)
        .set("user_name", "Hugo Feteira")
        .set("user_email", "hf@admin.pt")
        .set("userpwd", DigestUtils.sha512Hex("Password123!"))
        .set("user_phone", "912345678")
        .set("user_profile", "Administrador do sistema")
        .set("user_creation_time", Timestamp.now())
        .set("user_role", "ADMIN")
        .set("user_account_status", "ATIVADO")
        .set("user_citizen_card_number", "123456789")
        .set("user_nif", "123456789")
        .set("user_employer_entity", "Universidade Nova de Lisboa")
        .set("user_function", "Professor")
        .set("user_address", "Rua da Escola")
        .set("user_employer_nif", "987654321")
        .set("user_photo", "https://example.com/photo.jpg")
        .build();

    datastore.put(rootUser);
    LOG.severe("Root ADMIN user created successfully.");
  }
}
