-- Gym-Tracker: V7, картинки упражнений на ноги и пресс
-- Файлы лежат во frontend/public/exercises.
-- Без картинок пока: «Сведение ног в тренажёре», «Подъём на носки стоя», «Русские скручивания».

-- Упражнение с картинки, которого не было в V2
INSERT INTO exercises (name, description, load_type) VALUES
    ('Скручивания на наклонной скамье', 'Ноги закреплены, подъём корпуса на скамье с наклоном вниз.', 'bodyweight');

INSERT INTO exercise_muscles (exercise_id, muscle_id, role)
SELECT e.id, m.id, 'primary'
FROM exercises e
JOIN muscles m ON m.region_key = 'abs'
WHERE e.name = 'Скручивания на наклонной скамье' AND NOT e.is_custom;

UPDATE exercises e
SET image_url = v.image_url
FROM (VALUES
    -- Ноги
    ('Приседания со штангой',           '/exercises/barbell-squat.webp'),
    ('Жим ногами',                      '/exercises/leg-press.webp'),
    ('Выпады с гантелями',              '/exercises/dumbbell-lunge.webp'),
    ('Сгибание ног в тренажёре',        '/exercises/leg-curl.webp'),
    ('Разгибание ног в тренажёре',      '/exercises/leg-extension.webp'),
    ('Румынская тяга',                  '/exercises/romanian-deadlift.webp'),
    ('Ягодичный мост со штангой',       '/exercises/barbell-hip-thrust.webp'),
    -- Пресс
    ('Скручивания',                     '/exercises/crunch.webp'),
    ('Подъём ног в висе',               '/exercises/hanging-leg-raise.webp'),
    ('Скручивания на наклонной скамье', '/exercises/decline-crunch.webp')
) AS v (name, image_url)
WHERE e.name = v.name AND NOT e.is_custom;
