-- Gym-Tracker: V6, библиотека тренировок
-- Тренировка («шаблон») становится самостоятельной сущностью и подключается к любому числу планов.
--   plan_workouts          -> workout_templates      (название тренировки)
--   plan_workout_exercises -> workout_template_exercises
--   день недели            -> plan_days (план + тренировка + день)
-- Существующие данные переносятся: каждая строка plan_workouts становится отдельным шаблоном.
-- workouts.plan_workout_id заменяется на workout_template_id.

-- =====================================================================
-- Шаблоны тренировок
-- =====================================================================

-- legacy_plan_workout_id нужен только для переноса данных, в конце миграции колонка удаляется
CREATE TABLE workout_templates (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                   TEXT        NOT NULL,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    legacy_plan_workout_id BIGINT
);

INSERT INTO workout_templates (name, legacy_plan_workout_id)
SELECT name, id FROM plan_workouts;

-- Упражнение в шаблоне: подходы и диапазон повторов по умолчанию. Вес берётся из exercises.working_weight.
-- Одно упражнение один раз: при старте тренировки шаблон копируется в workout_exercises,
-- где есть UNIQUE (workout_id, exercise_id).
CREATE TABLE workout_template_exercises (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workout_template_id BIGINT   NOT NULL REFERENCES workout_templates(id) ON DELETE CASCADE,
    exercise_id         BIGINT   NOT NULL REFERENCES exercises(id),
    sort_order          SMALLINT NOT NULL DEFAULT 0,
    target_sets         SMALLINT NOT NULL CHECK (target_sets > 0),
    reps_min            SMALLINT CHECK (reps_min > 0),   -- 8–12; для точного числа min = max
    reps_max            SMALLINT CHECK (reps_max > 0),
    CONSTRAINT uq_workout_template_exercises UNIQUE (workout_template_id, exercise_id),
    CHECK (reps_min IS NULL OR reps_max IS NULL OR reps_min <= reps_max)
);
CREATE INDEX ix_wte_exercise ON workout_template_exercises (exercise_id);

INSERT INTO workout_template_exercises
    (workout_template_id, exercise_id, sort_order, target_sets, reps_min, reps_max)
SELECT t.id, pwe.exercise_id, pwe.sort_order, pwe.target_sets, pwe.reps_min, pwe.reps_max
FROM plan_workout_exercises pwe
JOIN workout_templates t ON t.legacy_plan_workout_id = pwe.plan_workout_id;

-- =====================================================================
-- Дни плана: какая тренировка в какой день
-- =====================================================================

-- Один шаблон можно ставить в разные дни и в разные планы; в один день плана - одна тренировка.
-- Шаблон, который стоит хотя бы в одном плане, удалить нельзя (RESTRICT).
-- Дни без тренировки = дни отдыха.
CREATE TABLE plan_days (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_id             BIGINT   NOT NULL REFERENCES plans(id) ON DELETE CASCADE,
    workout_template_id BIGINT   NOT NULL REFERENCES workout_templates(id) ON DELETE RESTRICT,
    day_of_week         SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),  -- 1 = Пн
    CONSTRAINT uq_plan_days_day UNIQUE (plan_id, day_of_week)
);
CREATE INDEX ix_plan_days_template ON plan_days (workout_template_id);

INSERT INTO plan_days (plan_id, workout_template_id, day_of_week)
SELECT pw.plan_id, t.id, pw.day_of_week
FROM plan_workouts pw
JOIN workout_templates t ON t.legacy_plan_workout_id = pw.id;

-- =====================================================================
-- Проведённые тренировки ссылаются на шаблон
-- =====================================================================

-- NULL = свободная тренировка (или шаблон потом удалён: история остаётся, название скопировано в workouts.name)
ALTER TABLE workouts
    ADD COLUMN workout_template_id BIGINT REFERENCES workout_templates(id) ON DELETE SET NULL;

UPDATE workouts w
SET workout_template_id = t.id
FROM workout_templates t
WHERE t.legacy_plan_workout_id = w.plan_workout_id;

CREATE INDEX ix_workouts_template ON workouts (workout_template_id);

-- Вместе с колонкой удаляется и внешний ключ на plan_workouts
ALTER TABLE workouts DROP COLUMN plan_workout_id;

-- =====================================================================
-- Уборка
-- =====================================================================

DROP TABLE plan_workout_exercises;
DROP TABLE plan_workouts;

ALTER TABLE workout_templates DROP COLUMN legacy_plan_workout_id;
