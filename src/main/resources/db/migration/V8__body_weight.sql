CREATE TABLE body_weights (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    measured_on DATE         NOT NULL UNIQUE,
    weight      NUMERIC(5,2) NOT NULL CHECK (weight > 0),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE profile (
    id                 SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    target_body_weight NUMERIC(5,2) CHECK (target_body_weight > 0)
);
INSERT INTO profile (id) VALUES (1);

CREATE INDEX ix_workout_sets_working ON workout_sets (workout_exercise_id) WHERE NOT is_warmup;
