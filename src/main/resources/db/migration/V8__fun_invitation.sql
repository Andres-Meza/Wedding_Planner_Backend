-- V8: segunda invitación ("invitación divertida") con enlace individual.
--
-- Decisiones de modelado:
--   * El token de la invitación formal (invitation.unique_token) es de toda la
--     invitación (persona o familia) y NO se toca: los enlaces ya enviados
--     siguen funcionando. La invitación divertida es individual, así que su
--     token vive en guest.fun_token (uno por invitado, independiente del
--     formal). No se crean invitados duplicados.
--   * El estado del cuestionario vive en fun_quiz (una fila por invitado) y
--     cada respuesta en fun_quiz_answer. Un invitado sin fila en fun_quiz
--     "no ha iniciado"; con fila IN_PROGRESS "iniciado"; COMPLETED
--     "completado". Así los tres estados son excluyentes por construcción.
--   * question_key / option_key son claves estables definidas en el catálogo
--     del backend (FunQuizCatalog); el texto de cada opción NO se guarda, de
--     modo que se puede reescribir sin migrar datos.
--
-- Migración solo aditiva: no borra ni modifica datos existentes. Los
-- invitados que ya existen reciben su token con gen_random_uuid() (incluida
-- en PostgreSQL desde la versión 13).

ALTER TABLE guest
  ADD COLUMN fun_token UUID;

UPDATE guest
SET fun_token = gen_random_uuid()
WHERE fun_token IS NULL;

ALTER TABLE guest
  ALTER COLUMN fun_token SET NOT NULL;

ALTER TABLE guest
  ADD CONSTRAINT uq_guest_fun_token UNIQUE (fun_token);

CREATE TABLE fun_quiz
(
  id           BIGSERIAL PRIMARY KEY,
  guest_id     BIGINT      NOT NULL UNIQUE REFERENCES guest (id) ON DELETE CASCADE,
  status       VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'
    CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
  started_at   TIMESTAMP   NOT NULL DEFAULT now(),
  updated_at   TIMESTAMP   NOT NULL DEFAULT now(),
  completed_at TIMESTAMP,
  result_key   VARCHAR(40),
  -- Coherencia: está completado si y solo si tiene fecha y resultado.
  CONSTRAINT chk_fun_quiz_completed_consistent
    CHECK ((status = 'COMPLETED') = (completed_at IS NOT NULL AND result_key IS NOT NULL))
);

-- Acelera los conteos por estado de las estadísticas.
CREATE INDEX idx_fun_quiz_status
  ON fun_quiz (status);

CREATE TABLE fun_quiz_answer
(
  id           BIGSERIAL PRIMARY KEY,
  quiz_id      BIGINT      NOT NULL REFERENCES fun_quiz (id) ON DELETE CASCADE,
  question_key VARCHAR(40) NOT NULL,
  option_key   VARCHAR(40) NOT NULL,
  answered_at  TIMESTAMP   NOT NULL DEFAULT now(),
  -- Una sola respuesta por pregunta: guardar de nuevo la reemplaza.
  CONSTRAINT uq_fun_quiz_answer_question UNIQUE (quiz_id, question_key)
);
