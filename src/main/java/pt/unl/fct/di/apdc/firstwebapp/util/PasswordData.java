package pt.unl.fct.di.apdc.firstwebapp.util;

public class PasswordData {

  public String password;
  public String newPassword;
  public String confirmation;

  public PasswordData() {
  }

  public PasswordData(String password, String newPassword, String confirmation) {
    this.password = password;
    this.newPassword = newPassword;
    this.confirmation = confirmation;
  }

}
