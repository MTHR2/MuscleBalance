-- Gym-Tracker: V1, начальная схема (PostgreSQL 15+, Flyway)
-- В V1 один пользователь без входа, поэтому user_id нигде нет.
-- Пользователи добавятся отдельной миграцией: таблица users + user_id в plans, workouts, exercises.

-- =====================================================================
-- Мышцы
-- =====================================================================

-- Крупные группы для карточек в фильтре: «Ноги», «Спина», «Грудь»...
CREATE TABLE muscle_groups (
    id         SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       TEXT     NOT NULL UNIQUE,
    image_url  TEXT,                                 -- часть тела с выделенной группой
    sort_order SMALLINT NOT NULL DEFAULT 0
);

-- Отдельные мышцы (~15): с ними работают тело на главной, цели плана и статистика
CREATE TABLE muscles (
    id              SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    muscle_group_id SMALLINT NOT NULL REFERENCES muscle_groups(id),
    name            TEXT     NOT NULL UNIQUE,
    region_key      TEXT     NOT NULL UNIQUE,        -- ключ области на SVG тела, например 'quads'
    image_url       TEXT,
    sort_order      SMALLINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_muscles_group ON muscles (muscle_group_id);

-- =====================================================================
-- Упражнения
-- =====================================================================

CREATE TABLE exercises (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           TEXT    NOT NULL,
    description    TEXT,
    load_type      TEXT    NOT NULL DEFAULT 'weighted'
                   CHECK (load_type IN ('weighted', 'bodyweight')),
                   -- bodyweight: weight в подходе = доп. вес (0 = просто свой вес)
    is_custom      BOOLEAN NOT NULL DEFAULT FALSE,   -- создано пользователем («Мои упражнения»)
    is_favorite    BOOLEAN NOT NULL DEFAULT FALSE,   -- сердечко на карточке
    working_weight NUMERIC(6,2) CHECK (working_weight >= 0),  -- кг; обновляется после тренировки
    archived_at    TIMESTAMPTZ,                      -- «удалено»: скрыто из списков, история остаётся
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- удалять можно только свои упражнения
    CHECK (archived_at IS NULL OR is_custom)
);
-- Без дублей имён среди неудалённых
CREATE UNIQUE INDEX uq_exercises_name ON exercises (lower(name)) WHERE archived_at IS NULL;

-- Упражнение <-> мышцы. «Комплексное» = больше одной строки.
-- Хотя бы одна основная мышца: проверяется в сервисе.
CREATE TABLE exercise_muscles (
    exercise_id BIGINT   NOT NULL REFERENCES exercises(id) ON DELETE CASCADE,
    muscle_id   SMALLINT NOT NULL REFERENCES muscles(id),
    role        TEXT     NOT NULL CHECK (role IN ('primary', 'secondary')),
                -- в статистике secondary = 0.5 подхода
    PRIMARY KEY (exercise_id, muscle_id)
);
CREATE INDEX ix_exercise_muscles_muscle ON exercise_muscles (muscle_id);

-- =====================================================================
-- Планы
-- =====================================================================

CREATE TABLE plans (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                TEXT     NOT NULL,
    description         TEXT,
    is_active           BOOLEAN  NOT NULL DEFAULT FALSE,
    default_weekly_sets SMALLINT CHECK (default_weekly_sets >= 0),  -- общая цель; NULL = нет
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
-- Активный план максимум один
CREATE UNIQUE INDEX uq_plans_one_active ON plans (is_active) WHERE is_active;

-- Цель на конкретную мышцу, перекрывает общую (0 = мышцу в плане не качаем)
CREATE TABLE plan_muscle_targets (
    plan_id     BIGINT   NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    muscle_id   SMALLINT NOT NULL REFERENCES muscles(id),
    weekly_sets SMALLINT NOT NULL CHECK (weekly_sets >= 0),
    PRIMARY KEY (plan_id, muscle_id)
);

-- Тренировка плана: один день недели, в день не больше одной.
-- Дни без тренировки = дни отдыха.
CREATE TABLE plan_workouts (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_id     BIGINT   NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    name        TEXT     NOT NULL,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),  -- 1 = Пн
    CONSTRAINT uq_plan_workouts_day UNIQUE (plan_id, day_of_week)
);

-- Упражнение в тренировке плана: подходы + диапазон повторов. Вес берётся из exercises.working_weight.
CREATE TABLE plan_workout_exercises (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_workout_id BIGINT   NOT NULL REFERENCES plan_workouts(id) ON DELETE CASCADE,
    exercise_id     BIGINT   NOT NULL REFERENCES exercises(id),
    sort_order      SMALLINT NOT NULL DEFAULT 0,
    target_sets     SMALLINT NOT NULL CHECK (target_sets > 0),
    reps_min        SMALLINT CHECK (reps_min > 0),   -- 8–12; для точного числа min = max
    reps_max        SMALLINT CHECK (reps_max > 0),
    CHECK (reps_min IS NULL OR reps_max IS NULL OR reps_min <= reps_max)
);
CREATE INDEX ix_pwe_plan_workout ON plan_workout_exercises (plan_workout_id);
CREATE INDEX ix_pwe_exercise     ON plan_workout_exercises (exercise_id);

-- =====================================================================
-- Проведённые тренировки
-- =====================================================================

CREATE TABLE workouts (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_workout_id BIGINT REFERENCES plan_workouts(id) ON DELETE SET NULL,  -- NULL = свободная
    name            TEXT NOT NULL,          -- копия названия на момент старта, для итогов и истории
    started_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    finished_at     TIMESTAMPTZ,            -- NULL = идёт сейчас
    CHECK (finished_at IS NULL OR finished_at >= started_at)
);
CREATE INDEX ix_workouts_started ON workouts (started_at DESC);
-- Незавершённая тренировка максимум одна
CREATE UNIQUE INDEX uq_workouts_one_open ON workouts ((finished_at IS NULL)) WHERE finished_at IS NULL;

-- Снимок плана при старте + упражнения, добавленные по ходу.
-- planned_sets: из плана, не меняется; по нему считается процент. NULL = добавлено по ходу, в процент не идёт.
-- target_sets: текущее число подходов на экране, можно увеличивать/уменьшать.
CREATE TABLE workout_exercises (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workout_id   BIGINT   NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    exercise_id  BIGINT   NOT NULL REFERENCES exercises(id),
    sort_order   SMALLINT NOT NULL DEFAULT 0,
    planned_sets SMALLINT CHECK (planned_sets > 0),
    target_sets  SMALLINT NOT NULL CHECK (target_sets >= 0),
    reps_min     SMALLINT CHECK (reps_min > 0),
    reps_max     SMALLINT CHECK (reps_max > 0),
    CONSTRAINT uq_workout_exercises UNIQUE (workout_id, exercise_id),
    CHECK (reps_min IS NULL OR reps_max IS NULL OR reps_min <= reps_max)
);
CREATE INDEX ix_workout_exercises_exercise ON workout_exercises (exercise_id);

-- Подход: каждое нажатие «+» = строка. Вес в кг, его можно менять прямо на тренировке.
CREATE TABLE workout_sets (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workout_exercise_id BIGINT   NOT NULL REFERENCES workout_exercises(id) ON DELETE CASCADE,
    set_number          SMALLINT NOT NULL CHECK (set_number > 0),
    reps                SMALLINT NOT NULL CHECK (reps > 0),
    weight              NUMERIC(6,2) NOT NULL DEFAULT 0 CHECK (weight >= 0),
    is_warmup           BOOLEAN  NOT NULL DEFAULT FALSE,  -- не идёт в статистику и рекорды
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_workout_sets_number UNIQUE (workout_exercise_id, set_number)  -- защита от двойного тапа
);