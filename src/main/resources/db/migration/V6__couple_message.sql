-- V6: mensaje opcional de los invitados para los novios.
--
-- Decisión de modelado: el mensaje es único por invitación (una persona o
-- toda una familia escribe un solo mensaje), así que vive como columna de
-- "invitation" y no en una tabla aparte. Guardar de nuevo es un UPDATE de la
-- misma fila, por lo que nunca se duplica. Las invitaciones existentes
-- quedan con mensaje vacío (NULL); no se toca ninguna otra columna.
--
-- couple_message             texto plano, máximo 1000 caracteres; NULL = sin mensaje.
-- couple_message_updated_at  fecha y hora de la última vez que se guardó.

ALTER TABLE invitation
ADD COLUMN couple_message VARCHAR(1000),
ADD COLUMN couple_message_updated_at TIMESTAMP;

-- Coherencia: hay fecha si y solo si hay mensaje.
ALTER TABLE invitation
ADD CONSTRAINT chk_invitation_couple_message_consistent CHECK (
    (couple_message IS NULL) = (
        couple_message_updated_at IS NULL
    )
);

-- Acelera el listado de la pantalla "Mensajes de los invitados"
-- (más recientes primero, solo invitaciones que sí tienen mensaje).
CREATE INDEX idx_invitation_couple_message_updated_at ON invitation (
    couple_message_updated_at DESC
)
WHERE
    couple_message IS NOT NULL;