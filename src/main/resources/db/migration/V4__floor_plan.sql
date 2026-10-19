-- V4: plano del salón editable.
--
-- floor_plan_element guarda cada pieza del plano (mesas, arco, pista de baile,
-- escenario, árboles, etc.) con su posición, tamaño y rotación. Las mesas se
-- enlazan con table_wedding, así el plano sigue conectado con la lista de
-- invitados y las asignaciones que ya existen.

CREATE TABLE floor_plan_element (
    id BIGSERIAL PRIMARY KEY,
    kind VARCHAR(20) NOT NULL CHECK (
        kind IN (
            'TABLE_ROUND',
            'TABLE_RECT',
            'ARCH',
            'AISLE',
            'DANCE_FLOOR',
            'STAGE',
            'RESTROOM',
            'TREE',
            'PIANO',
            'GIFT_TABLE',
            'CAKE_TABLE',
            'TEXT'
        )
    ),
    x DOUBLE PRECISION NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    width DOUBLE PRECISION NOT NULL CHECK (width > 0),
    height DOUBLE PRECISION NOT NULL CHECK (height > 0),
    rotation DOUBLE PRECISION NOT NULL DEFAULT 0,
    label VARCHAR(100),
    -- Una mesa aparece una sola vez en el plano. Si se borra la mesa, su pieza
    -- desaparece con ella.
    table_id BIGINT UNIQUE REFERENCES table_wedding (id) ON DELETE CASCADE
);

-- Silla concreta que ocupa cada invitado en su mesa (0 = primera silla).
-- Es opcional: las asignaciones que ya existen (o las automáticas) quedan
-- sin silla fija y el plano las acomoda en las sillas libres.
ALTER TABLE table_assignment
ADD COLUMN seat_index INTEGER CHECK (seat_index >= 0);

CREATE UNIQUE INDEX uq_table_assignment_seat ON table_assignment (table_id, seat_index)
WHERE
    seat_index IS NOT NULL;