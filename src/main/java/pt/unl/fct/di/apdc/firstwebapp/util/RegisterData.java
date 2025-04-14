package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.regex.Pattern;

public class RegisterData {

  public String username;
  public String password;
  public String confirmation;
  public String email;
  public String name;
  public String phoneNumber;
  public String profile;

  public String citizenCardNumber;
  public String role;
  public String nif;
  public String employer;
  public String job;
  public String address;
  public String employerNif;
  public String accountStatus;
  public String photo;

  public RegisterData() {
  }

  public RegisterData(String username, String password, String confirmation, String email, String name,
      String phoneNumber, String profile, String citizenCardNumber, String role,
      String nif, String employer, String job, String address, String employerNif,
      String accountStatus, String photo) {

    this.username = username;
    this.password = password;
    this.confirmation = confirmation;
    this.email = email;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.profile = profile;

    this.citizenCardNumber = citizenCardNumber;
    this.role = role;
    this.nif = nif;
    this.employer = employer;
    this.job = job;
    this.address = address;
    this.employerNif = employerNif;
    this.accountStatus = accountStatus;
    this.photo = photo;
  }

  private boolean nonEmptyOrBlankField(String field) {
    return field != null && !field.isBlank();
  }

  private boolean validEmail(String email) {
    String emailRegex = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    return Pattern.matches(emailRegex, email);
  }

  private boolean validPassword(String password) {
    // 8 caracteres.
    String pwdRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\p{Punct}]).{8,}$";
    return Pattern.matches(pwdRegex, password);
  }

  private boolean validAccountStatus(String status) {
    return status.equals("ATIVADA") || status.equals("SUSPENSA") || status.equals("DESATIVADA");
  }

  private boolean validRole(String role) {
    return role.equals("ENDUSER") || role.equals("BACKOFFICE") || role.equals("ADMIN") || role.equals("PARTNER");
  }

  public boolean validRegistration() {
    return nonEmptyOrBlankField(username) &&
        nonEmptyOrBlankField(password) &&
        nonEmptyOrBlankField(confirmation) &&
        nonEmptyOrBlankField(email) &&
        nonEmptyOrBlankField(name) &&
        nonEmptyOrBlankField(phoneNumber) &&
        nonEmptyOrBlankField(profile) &&
        validEmail(email) &&
        password.equals(confirmation) &&
        validPassword(password) &&
        validAccountStatus(accountStatus) &&
        validRole(role);
  }

  public String getRole() {
    return role;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getState() {
    return accountStatus;
  }

  public String getEmail() {
    return email;
  }
}
