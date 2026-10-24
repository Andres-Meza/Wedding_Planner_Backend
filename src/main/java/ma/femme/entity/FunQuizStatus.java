package ma.femme.entity;

// Estados guardados en base de datos. "No iniciado" no se guarda: es la
// ausencia de una fila en fun_quiz (ver FunQuizState para el estado completo).
public enum FunQuizStatus {
  IN_PROGRESS,
  COMPLETED
}
