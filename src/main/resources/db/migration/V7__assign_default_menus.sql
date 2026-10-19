-- V7: asigna el menú por defecto a los invitados que todavía no tienen uno.
--
-- Reglas (las mismas que aplica el backend al crear invitados):
--   * adultos  (is_minor = FALSE) -> menú de adultos llamado "Menú 1"
--   * menores  (is_minor = TRUE)  -> menú infantil llamado "Menú Infantil 1"
--
-- Estrategia segura:
--   * Solo se tocan los invitados con menu_id IS NULL. Cualquier menú ya
--     elegido se conserva tal cual (incluso si el menú hoy está inactivo).
--   * Solo se asigna si el menú por defecto EXISTE, está ACTIVO y su
--     categoría (is_kids) coincide con la del invitado. Si no cumple, la
--     migración no hace nada y no elige ningún otro menú.
--   * No crea menús: los menús no están garantizados en todos los entornos.
--     Si todavía no existen, esta migración no cambia nada y el backend
--     asigna el menú por defecto cuando el invitado confirme su asistencia.
--   * No modifica attendance, response_date, responded_by ni ninguna
--     respuesta RSVP; no borra ni cambia relaciones existentes.
--
-- Importante: los nombres deben coincidir con app.menus.default-adult-name y
-- app.menus.default-kids-name (application.yml). La comparación no distingue
-- mayúsculas de minúsculas.

UPDATE guest g
SET
    menu_id = m.id
FROM menu m
WHERE
    g.menu_id IS NULL
    AND m.active = TRUE
    AND m.is_kids = g.is_minor
    AND lower(m.name) = CASE
        WHEN g.is_minor THEN lower('Menú Infantil 1')
        ELSE lower('Menú 1')
    END;