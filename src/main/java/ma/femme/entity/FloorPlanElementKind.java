package ma.femme.entity;

// Tipos de pieza que se pueden colocar en el plano del salón.
public enum FloorPlanElementKind {
  TABLE_ROUND,
  TABLE_RECT,
  ARCH,
  AISLE,
  DANCE_FLOOR,
  STAGE,
  RESTROOM,
  TREE,
  PIANO,
  GIFT_TABLE,
  CAKE_TABLE,
  TEXT;

  public boolean isTable() {
    return this == TABLE_ROUND || this == TABLE_RECT;
  }
}
