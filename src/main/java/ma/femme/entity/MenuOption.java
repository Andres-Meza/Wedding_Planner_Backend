package ma.femme.entity;

// Opciones de menú para invitados adultos.
public enum MenuOption {
    CHICKEN("Pollo"),
    BEEF("Carne de res"),
    FISH("Pescado"),
    VEGETARIAN("Vegetariano");

    private final String label;

    MenuOption(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
