CREATE TABLE invitation
(
    id             BIGSERIAL PRIMARY KEY,
    type           VARCHAR(20)  NOT NULL CHECK (type IN ('INDIVIDUAL', 'FAMILY')),
    unique_token   UUID         NOT NULL UNIQUE,
    reference_name VARCHAR(150) NOT NULL,
    contact_phone  VARCHAR(30),
    contact_email  VARCHAR(150),
    status         VARCHAR(25)  NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PARTIALLY_RESPONDED', 'FULLY_RESPONDED')),
    creation_date  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE guest
(
    id                  BIGSERIAL PRIMARY KEY,
    invitation_id       BIGINT       NOT NULL REFERENCES invitation (id) ON DELETE CASCADE,
    full_name           VARCHAR(150) NOT NULL,
    is_minor            BOOLEAN      NOT NULL DEFAULT FALSE,
    attendance          VARCHAR(20)  NOT NULL DEFAULT 'NO_RESPONSE'
        CHECK (attendance IN ('NO_RESPONSE', 'CONFIRMED', 'NOT_ATTENDING')),
    responded_by        VARCHAR(150),
    dietary_restriction VARCHAR(200),
    response_date       TIMESTAMP
);

CREATE INDEX idx_guest_invitation_id
    ON guest (invitation_id);

CREATE TABLE table_wedding
(
    id             BIGSERIAL PRIMARY KEY,
    name_or_number VARCHAR(50) NOT NULL,
    capacity       INTEGER     NOT NULL CHECK (capacity > 0),
    area           VARCHAR(100)
);

CREATE TABLE table_assignment
(
    id                BIGSERIAL PRIMARY KEY,
    guest_id          BIGINT  NOT NULL UNIQUE REFERENCES guest (id) ON DELETE CASCADE,
    table_id          BIGINT  NOT NULL REFERENCES table_wedding (id) ON DELETE RESTRICT,
    manually_assigned BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_table_assignment_table_id
    ON table_assignment (table_id);
