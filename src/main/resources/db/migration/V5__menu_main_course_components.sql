-- V5: el plato fuerte pasa de un único texto (main_course) a sus componentes:
-- proteína 1, proteína 2, acompañamiento y ensalada.
--
-- Estrategia para los menús que ya existen:
--   * El texto original NO se puede dividir de forma automática y confiable
--     (puede ser "Pollo", "Pollo y carne con arroz", etc.), así que se
--     conserva completo en protein_1. Nada se pierde y los menús que ya
--     eligieron invitados siguen siendo los mismos (mismo id).
--   * protein_2, accompaniment y salad quedan en NULL hasta que el admin
--     edite el menú. Editar un menú de adultos exige completar los cuatro.
--   * main_course se renombra a legacy_main_course (y deja de ser NOT NULL)
--     como respaldo del valor original, para revisar la transición. La
--     aplicación ya no la lee ni la escribe. Ver docs/menu-main-course-migration.md.
--
-- Esta migración es solo aditiva: no borra datos ni toca guest.menu_id.

ALTER TABLE menu
ADD COLUMN protein_1 VARCHAR(200),
ADD COLUMN protein_2 VARCHAR(200),
ADD COLUMN accompaniment VARCHAR(200),
ADD COLUMN salad VARCHAR(200);

UPDATE menu SET protein_1 = main_course;

ALTER TABLE menu ALTER COLUMN protein_1 SET NOT NULL;

ALTER TABLE menu RENAME COLUMN main_course TO legacy_main_course;

ALTER TABLE menu ALTER COLUMN legacy_main_course DROP NOT NULL;

COMMENT ON COLUMN menu.legacy_main_course IS 'Respaldo del plato fuerte original (antes de V5). Ya no lo usa la aplicación; se puede eliminar en una migración futura cuando todos los menús estén revisados.';