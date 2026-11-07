# MuscleBalance

Дневник силовых тренировок с тепловой картой нагрузки на мышцы. Приложение показывает, какие группы мышц за неделю проработаны и насколько, ведёт тренировки по шаблонам и плану и строит аналитику по рабочим весам, количеству тренировок и весу тела.

Приложение рассчитано на одного пользователя, авторизации нет.

## Возможности

- **Главная.** Тепловая карта тела за неделю: цвет мышцы показывает долю выполненных подходов от цели. Ниже карточка тренировки на сегодня и итоги недели.
- **Упражнения.** Каталог из 40 упражнений с картинками, поиск, фильтр по группе мышц, избранное, свои упражнения с основными и вспомогательными мышцами.
- **Тренировки.** Шаблоны тренировок, свободная тренировка без шаблона, история с длительностью, подходами, объёмом и процентом выполнения плана.
- **Экран тренировки.** Подходы с повторами и весом, разминочные подходы, изменение числа подходов по ходу. После завершения рабочий вес упражнения обновляется.
- **План.** Шаблоны по дням недели, цели по подходам в неделю на каждую мышцу, один активный план.
- **Аналитика.** Рабочий вес по неделям с расчётным максимумом, нагрузка по группам мышц, тренировки по месяцам против плана, вес тела со средним за 7 дней и целью, личные рекорды.

Вспомогательная мышца засчитывается как 0,5 подхода, разминочные подходы не учитываются.

## Стек

| Часть | Технологии |
|---|---|
| Бэкенд | Java 21, Spring Boot 4, Spring Data JPA, Hibernate 7, Bean Validation, Lombok |
| База | PostgreSQL 17, миграции Flyway |
| Фронтенд | React 19, React Router 7, TypeScript, Vite, обычный CSS |
| Тесты | JUnit 5, MockMvc, Testcontainers |
| Запуск | Docker Compose, nginx для фронтенда |

## Быстрый старт в Docker

Нужен только Docker.

1. Скопируйте пример настроек и задайте пароль базы:

   ```bash
   cp .env.example .env
   ```

   В `.env` заполните `DB_PASSWORD`. Если нужен ваш часовой пояс, укажите `APP_TIMEZONE`, например `Europe/Moscow`. Без него сервер считает дни и недели по UTC.

2. Соберите и запустите:

   ```bash
   docker compose up -d --build
   ```

3. Откройте http://localhost:3000.

Поднимаются три контейнера:

| Сервис | Контейнер | Что делает |
|---|---|---|
| `postgres` | `gymtracker-db` | PostgreSQL, данные в томе `pgdata`, порт `DB_PORT` открыт наружу |
| `app` | `musclebalance-app` | Spring Boot, при старте применяет миграции |
| `web` | `musclebalance-web` | nginx: отдаёт фронтенд и проксирует `/api` в `app` |

Остановить: `docker compose down`. Данные остаются в томе `pgdata`. Удалить их вместе с томом: `docker compose down -v`.

## Локальная разработка

Нужны JDK 21 или новее, Node.js 24 и Docker для базы.

1. Создайте `.env` из `.env.example`, как в быстром старте.
2. Запустите только базу:

   ```bash
   docker compose up -d postgres
   ```

3. Запустите бэкенд на порту 8080. Настройки он берёт из `.env` в корне проекта.

   ```bash
   ./mvnw spring-boot:run
   ```

4. В другом терминале запустите фронтенд на порту 5173. Vite проксирует `/api` на `localhost:8080`.

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

5. Откройте http://localhost:5173.

Проверки фронтенда: `npm run build` (TypeScript и сборка) и `npm run lint` (oxlint).

## Настройки

Все переменные читаются из `.env`.

| Переменная | По умолчанию в примере | Назначение |
|---|---|---|
| `DB_HOST` | `localhost` | хост базы для локального запуска; в Docker подставляется `postgres` |
| `DB_PORT` | `5433` | порт базы на хосте; в Docker бэкенд ходит на внутренний `5432` |
| `DB_NAME` | `gymtracker` | имя базы |
| `DB_USER` | `gymtracker` | пользователь базы |
| `DB_PASSWORD` | пусто | пароль базы, обязателен |
| `APP_TIMEZONE` | пусто | часовой пояс для дней и недель, например `Europe/Moscow`; пусто значит часовой пояс системы |
| `WEB_PORT` | `3000` | порт фронтенда в Docker |

## Тесты

```bash
./mvnw test
```

Для тестов нужен запущенный Docker. Testcontainers поднимает отдельный временный PostgreSQL 17 и прогоняет на нём все миграции, ваша база не используется. Перед каждым тестом данные очищаются, поэтому порядок тестов не важен.

- `src/test/java/com/musclebalance/api`: сквозные тесты эндпоинтов через MockMvc с настоящими сервисами и базой.
- `src/test/java/com/musclebalance/exception`: тесты формата ошибок на контроллерах с подменёнными сервисами.

## Структура

```
src/main/java/com/musclebalance
  controller   REST-контроллеры
  service      бизнес-логика и статистика
  repository   Spring Data и запросы статистики
  entity       JPA-сущности
  dto          объекты запросов и ответов
  mapper       преобразование сущностей в DTO
  exception    ApiError, коды ошибок, исключения, глобальный обработчик
  config       часы и часовой пояс
src/main/resources/db/migration   миграции Flyway V1–V8
frontend/src
  pages        страницы
  analytics    графики аналитики
  heatmap      тепловая карта
  workout      экран тренировки
  home         карточки главной
  api.ts       типы и запросы к API
```

## API

Все адреса начинаются с `/api/v1`.

| Раздел | Эндпоинты |
|---|---|
| Мышцы | `GET /muscle-groups`, `GET /muscle-groups/{id}`, `GET /muscles` |
| Упражнения | `GET /exercises?q=&muscleGroupId=&favourite=&custom=`, `GET /exercises/{id}`, `POST /exercises`, `PUT /exercises/{id}/favourite` |
| Шаблоны | `GET /workout-templates`, `GET /workout-templates/{id}`, `POST /workout-templates` |
| Планы | `GET /plans`, `GET /plans/{id}`, `POST /plans`, `PUT /plans/{id}`, `PATCH /plans/{id}/activate`, `DELETE /plans/{id}`, `GET /plans/active/today` |
| Тренировки | `POST /workouts`, `GET /workouts?limit=`, `GET /workouts/active`, `GET /workouts/{id}`, `DELETE /workouts/{id}`, `POST /workouts/{id}/finish` |
| Упражнения в тренировке | `POST /workouts/{id}/exercises`, `PATCH /workouts/{id}/exercises/{weId}`, `DELETE /workouts/{id}/exercises/{weId}` |
| Подходы | `POST /workouts/{id}/exercises/{weId}/sets`, `PATCH .../sets/{setId}`, `DELETE .../sets/{setId}` |
| Статистика | `GET /stats/muscles?weekStart=`, `GET /stats/week?weekStart=`, `GET /stats/overview`, `GET /stats/muscle-groups`, `GET /stats/workouts/monthly?months=`, `GET /stats/exercises`, `GET /stats/exercises/{id}/progress`, `GET /stats/records?limit=` |
| Вес тела | `GET /body-weights`, `POST /body-weights`, `DELETE /body-weights/{id}` |
| Профиль | `GET /profile`, `PUT /profile` |

Эндпоинты статистики и веса тела принимают `period=MONTH|QUARTER|YEAR`.

Когда данных нет, `GET /workouts/active` и `GET /plans/active/today` отвечают `204 No Content`.

### Ошибки

Любая ошибка приходит в одном формате:

```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Проверьте заполнение полей",
  "path": "/api/v1/plans",
  "timestamp": "2026-11-07T09:08:53.966Z",
  "fieldErrors": [
    { "field": "days[0].dayOfWeek", "message": "должно быть не больше 7" }
  ]
}
```

| Статус | Когда |
|---|---|
| 400 | неверный JSON, тип параметра или нарушены ограничения полей (`fieldErrors`) |
| 404 | объект или адрес не найден |
| 409 | конфликт: имя занято, тренировка уже идёт или завершена, подход не последний |
| 422 | нарушено правило: мышца указана дважды, два плана на один день, нет рабочих подходов |
| 500 | внутренняя ошибка без подробностей в ответе, подробности в логе |

Поле `code` стабильно, на него можно опираться в клиенте. Все коды перечислены в `CommonErrorCode` и `DomainErrorCode`. Тексты сообщений полей зависят от заголовка `Accept-Language`.
