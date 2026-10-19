-- V3: menús completos (tabla menu) + alergias tipificadas.
--
-- Reemplaza guest.selected_menu (un VARCHAR con el nombre de un enum fijo)
-- por una relación real guest -> menu, conservando lo que los invitados ya
-- hayan elegido.

-- 1) Tabla de menús
CREATE TABLE menu (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    starter VARCHAR(200) NOT NULL,
    main_course VARCHAR(200) NOT NULL,
    dessert VARCHAR(200) NOT NULL,
    drink VARCHAR(200) NOT NULL,
    is_kids BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_menu_name ON menu (lower(name));

-- 2) Relación guest -> menu (RESTRICT: no se puede borrar un menú elegido)
ALTER TABLE guest
ADD COLUMN menu_id BIGINT REFERENCES menu (id) ON DELETE RESTRICT;

CREATE INDEX idx_guest_menu_id ON guest (menu_id);

-- 3) Migración de las elecciones existentes (enums MenuOption / KidsMenuOption).
--    Se crean temporalmente los 8 menús "legados" y se enlazan a los invitados
--    que ya habían elegido uno. Quedan INACTIVOS: el admin los edita con los
--    platos reales y los activa (o crea menús nuevos).
INSERT INTO
    menu (
        name,
        starter,
        main_course,
        dessert,
        drink,
        is_kids,
        active
    )
VALUES (
        'Pollo',
        'Por definir',
        'Pollo',
        'Por definir',
        'Por definir',
        FALSE,
        FALSE
    ),
    (
        'Carne de res',
        'Por definir',
        'Carne de res',
        'Por definir',
        'Por definir',
        FALSE,
        FALSE
    ),
    (
        'Pescado',
        'Por definir',
        'Pescado',
        'Por definir',
        'Por definir',
        FALSE,
        FALSE
    ),
    (
        'Vegetariano',
        'Por definir',
        'Vegetariano',
        'Por definir',
        'Por definir',
        FALSE,
        FALSE
    ),
    (
        'Nuggets de pollo',
        'Por definir',
        'Nuggets de pollo',
        'Por definir',
        'Por definir',
        TRUE,
        FALSE
    ),
    (
        'Pasta con salsa',
        'Por definir',
        'Pasta con salsa',
        'Por definir',
        'Por definir',
        TRUE,
        FALSE
    ),
    (
        'Pizza',
        'Por definir',
        'Pizza',
        'Por definir',
        'Por definir',
        TRUE,
        FALSE
    ),
    (
        'Plato de fruta',
        'Por definir',
        'Plato de fruta',
        'Por definir',
        'Por definir',
        TRUE,
        FALSE
    );

UPDATE guest g
SET
    menu_id = m.id
FROM (
        VALUES ('CHICKEN', 'Pollo'), ('BEEF', 'Carne de res'), ('FISH', 'Pescado'), ('VEGETARIAN', 'Vegetariano'), (
                'CHICKEN_NUGGETS', 'Nuggets de pollo'
            ), ('PASTA', 'Pasta con salsa'), ('PIZZA', 'Pizza'), (
                'FRUIT_PLATE', 'Plato de fruta'
            )
    ) AS legacy (code, menu_name)
    JOIN menu m ON m.name = legacy.menu_name
WHERE
    g.selected_menu = legacy.code;

-- Los menús legados que nadie eligió no aportan nada: se eliminan.
DELETE FROM menu m
WHERE
    NOT EXISTS (
        SELECT 1
        FROM guest g
        WHERE
            g.menu_id = m.id
    );

ALTER TABLE guest DROP COLUMN selected_menu;

-- 4) Alergias tipificadas. dietary_restriction se conserva como
--    "observaciones / restricciones" (texto libre).
ALTER TABLE guest
ADD COLUMN allergy_type VARCHAR(20) CHECK (
    allergy_type IN (
        'PEANUTS',
        'TREE_NUTS',
        'DAIRY',
        'GLUTEN',
        'SEAFOOD',
        'EGGS',
        'OTHER'
    )
);

-- Lo que ya se había escrito en dietary_restriction se preguntaba como
-- "alergia alimentaria": se marca como alergia de tipo "Otra" para que no
-- pase desapercibida; el admin puede reclasificarla después.
UPDATE guest
SET
    allergy_type = 'OTHER'
WHERE
    dietary_restriction IS NOT NULL
    AND btrim (dietary_restriction) <> '';