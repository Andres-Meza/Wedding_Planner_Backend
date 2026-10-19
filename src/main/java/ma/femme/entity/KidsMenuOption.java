package ma.femme.entity;

// Opciones de menú infantil — se usan cuando el invitado es menor de edad.
public enum KidsMenuOption {
  CHICKEN_NUGGETS("Nuggets de pollo"),
  PASTA("Pasta con salsa"),
  PIZZA("Pizza"),
  FRUIT_PLATE("Plato de fruta");

  private final String label;

  KidsMenuOption(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
