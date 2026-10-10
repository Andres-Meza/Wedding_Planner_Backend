package ma.femme.entity;

// Tipos de alergia alimentaria que el invitado puede reportar.
// "OTHER" exige una descripción en las observaciones.
public enum AllergyType {
  PEANUTS("Maní"),
  TREE_NUTS("Frutos secos"),
  DAIRY("Lácteos"),
  GLUTEN("Gluten"),
  SEAFOOD("Mariscos"),
  EGGS("Huevo"),
  OTHER("Otra");

  private final String label;

  AllergyType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
