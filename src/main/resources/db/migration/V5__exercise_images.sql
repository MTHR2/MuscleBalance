-- Gym-Tracker: V5, картинки упражнений и пресса
-- Файлы лежат во frontend/public/exercises и frontend/public/muscle-groups.

ALTER TABLE exercises ADD COLUMN image_url TEXT;

UPDATE muscle_groups SET image_url = '/muscle-groups/abs.webp' WHERE name = 'Пресс';

-- Упражнения, которые есть на картинках, но не было в V2
INSERT INTO exercises (name, description, load_type) VALUES
    ('Махи гантелями вперёд',     'Подъём гантелей перед собой до уровня плеч.',  'weighted'),
    ('Тяга гантелей к подбородку', 'Гантели вдоль корпуса вверх, локти выше кистей.', 'weighted');

INSERT INTO exercise_muscles (exercise_id, muscle_id, role)
SELECT e.id, m.id, em.role
FROM (VALUES
    ('Махи гантелями вперёд',      'front_delts', 'primary'),
    ('Тяга гантелей к подбородку', 'side_delts',  'primary'),
    ('Тяга гантелей к подбородку', 'traps',       'secondary')
) AS em (exercise_name, region_key, role)
JOIN exercises e ON e.name = em.exercise_name AND NOT e.is_custom
JOIN muscles   m ON m.region_key = em.region_key;

UPDATE exercises e
SET image_url = v.image_url
FROM (VALUES
    -- Грудь
    ('Жим штанги лёжа',                  '/exercises/bench-press.webp'),
    ('Жим гантелей на наклонной скамье', '/exercises/incline-dumbbell-press.webp'),
    ('Разведение гантелей лёжа',         '/exercises/dumbbell-fly.webp'),
    ('Сведение рук в кроссовере',        '/exercises/cable-crossover.webp'),
    ('Отжимания от пола',                '/exercises/push-up.webp'),
    ('Отжимания на брусьях',             '/exercises/dips.webp'),
    -- Спина
    ('Подтягивания',                     '/exercises/pull-up.webp'),
    ('Тяга верхнего блока',              '/exercises/lat-pulldown.webp'),
    ('Тяга штанги в наклоне',            '/exercises/barbell-row.webp'),
    ('Тяга гантели одной рукой',         '/exercises/one-arm-dumbbell-row.webp'),
    ('Тяга горизонтального блока',       '/exercises/seated-cable-row.webp'),
    ('Становая тяга',                    '/exercises/deadlift.webp'),
    ('Гиперэкстензия',                   '/exercises/hyperextension.webp'),
    ('Шраги с гантелями',                '/exercises/dumbbell-shrug.webp'),
    -- Плечи
    ('Жим штанги стоя',                  '/exercises/overhead-press.webp'),
    ('Жим гантелей сидя',                '/exercises/seated-dumbbell-press.webp'),
    ('Махи гантелями в стороны',         '/exercises/lateral-raise.webp'),
    ('Махи гантелями вперёд',            '/exercises/front-raise.webp'),
    ('Разведение гантелей в наклоне',    '/exercises/rear-delt-fly.webp'),
    ('Тяга штанги к подбородку',         '/exercises/upright-row.webp'),
    ('Тяга гантелей к подбородку',       '/exercises/dumbbell-upright-row.webp'),
    -- Руки
    ('Подъём штанги на бицепс',          '/exercises/barbell-curl.webp'),
    ('Подъём гантелей на бицепс',        '/exercises/dumbbell-curl.webp'),
    ('Молотки с гантелями',              '/exercises/hammer-curl.webp'),
    ('Французский жим лёжа',             '/exercises/skull-crusher.webp'),
    ('Разгибание рук на верхнем блоке',  '/exercises/triceps-pushdown.webp'),
    ('Жим штанги узким хватом',          '/exercises/close-grip-bench-press.webp')
) AS v (name, image_url)
WHERE e.name = v.name AND NOT e.is_custom;
