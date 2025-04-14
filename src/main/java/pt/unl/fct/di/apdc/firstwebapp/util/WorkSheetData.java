package pt.unl.fct.di.apdc.firstwebapp.util;

public class WorkSheetData {
  public String reference;
  public String description;
  public String targetType;
  public String adjudicationState;

  public String adjudicationDate;
  public String expectedStartDate;
  public String expectedEndDate;
  public String partnerUsername;
  public String companyName;
  public String companyNif;
  public String workStatus;
  public String notes;

  public WorkSheetData() {
  }

  public boolean isValidForCreation() {
    return reference != null && !reference.isBlank()
        && description != null && !description.isBlank()
        && targetType != null && (targetType.equals("Propriedade Pública") || targetType.equals("Propriedade Privada"))
        && adjudicationState != null
        && (adjudicationState.equals("ADJUDICADO") || adjudicationState.equals("NÃO ADJUDICADO"));
  }

  public boolean adjudicationFieldsAreValid() {
    return adjudicationState.equals("ADJUDICADO") &&
        adjudicationDate != null && expectedStartDate != null && expectedEndDate != null &&
        partnerUsername != null && companyName != null && companyNif != null &&
        workStatus != null
        && (workStatus.equals("NÃO INICIADO") || workStatus.equals("EM CURSO") || workStatus.equals("CONCLUÍDO"));
  }
}
