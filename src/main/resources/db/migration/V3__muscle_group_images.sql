-- Gym-Tracker: V3, картинки групп мышц
-- Пути относительные: файлы лежат во frontend/public/muscle-groups и отдаются фронтом.
-- Для «Пресса» картинки пока нет.

UPDATE muscle_groups g
SET image_url = v.image_url
FROM (VALUES
    ('Грудь', '/muscle-groups/chest.webp'),
    ('Спина', '/muscle-groups/back.webp'),
    ('Плечи', '/muscle-groups/shoulders.webp'),
    ('Руки',  '/muscle-groups/arms.webp'),
    ('Ноги',  '/muscle-groups/legs.webp')
) AS v(name, image_url)
WHERE g.name = v.name;
