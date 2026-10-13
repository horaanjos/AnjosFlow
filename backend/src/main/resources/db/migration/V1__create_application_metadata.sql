-- Marca técnica da fundação. Entidades de negócio serão adicionadas na Etapa 2.
CREATE TABLE application_metadata (
    id SMALLINT PRIMARY KEY CHECK (id = 1),
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO application_metadata (id, name) VALUES (1, 'AnjosFlow');
