# Ktor Users API — Advanced

Учебный проект на Kotlin + Ktor: REST CRUD, PostgreSQL, JWT, роли USER/ADMIN, WebSocket-события, Swagger/OpenAPI и Docker Compose.

## Возможности

- PostgreSQL с автоматическим созданием таблицы `app_users`.
- Регистрация и вход: `POST /api/auth/register`, `POST /api/auth/login`.
- JWT Bearer-аутентификация.
- Роли `USER` и `ADMIN`: обычный пользователь работает со своим профилем; список и удаление пользователей доступны только ADMIN.
- WebSocket `ws://localhost:8080/ws/notifications`: события `USER_UPDATED` и `USER_DELETED`.
- Swagger UI: `http://localhost:8080/swagger`; OpenAPI: `http://localhost:8080/openapi.yaml`.
- Централизованные ошибки и логирование запросов.

## Быстрый запуск через Docker

Требуются Docker Desktop и Docker Compose.

```bash
docker compose up --build
```

Откройте `http://localhost:8080/swagger`. Остановка: `docker compose down`. Удаление данных БД: `docker compose down -v`.

Перед реальным деплоем создайте `.env` на основе `.env.example` и задайте длинный случайный `JWT_SECRET`. Не используйте значения по умолчанию в production.

## Локальный запуск

Требуются JDK 17+ и Gradle 8.14+; PostgreSQL должен быть запущен.

PowerShell:
```powershell
$env:JDBC_DATABASE_URL="jdbc:postgresql://localhost:5432/ktor_users"
$env:DB_USER="postgres"
$env:DB_PASSWORD="postgres"
$env:JWT_SECRET="a-long-local-development-secret"
gradle run
```

Если в проекте есть Gradle Wrapper, используйте `.\gradlew.bat run` вместо `gradle run`.

## API

| Метод | Путь | Доступ |
|---|---|---|
| GET | `/api/health` | публичный |
| POST | `/api/auth/register` | публичный |
| POST | `/api/auth/login` | публичный |
| GET | `/api/users/me` | USER или ADMIN |
| PUT | `/api/users/me` | USER или ADMIN |
| GET | `/api/users` | ADMIN |
| DELETE | `/api/users/{id}` | ADMIN |
| WS | `/ws/notifications` | WebSocket |

### Примеры запросов

Регистрация:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"password123"}'
```

Вход:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"password123"}'
```

Используйте полученное значение `token` как `Authorization: Bearer <token>`.

## Роли

Публичная регистрация всегда создаёт `USER`. Назначение `ADMIN` делается оператором БД после регистрации:

```sql
UPDATE app_users SET role = 'ADMIN' WHERE username = 'student1';
```

После изменения роли войдите заново, чтобы получить JWT с обновлённой ролью.

## WebSocket

Подключитесь к `ws://localhost:8080/ws/notifications`. При обновлении профиля или удалении пользователя сервер рассылает JSON, например:

```json
{"type":"USER_UPDATED","userId":2,"username":"student2"}
```

## Архитектура

- `routes` — HTTP endpoints и проверка доступа.
- `auth` — JWT token service и principal helpers.
- `repository` — SQL операции с пользователями.
- `database` — HikariCP и PostgreSQL schema bootstrap.
- `websocket` — управление подключениями и broadcast.
- `plugins` — JSON serialization, call logging, StatusPages.
- `models` — DTO и API error models.

## Проверки и деплой

```bash
gradle test
docker compose config
docker compose up --build
```

Для публичного сервера используйте reverse proxy с HTTPS/WSS, надёжный `JWT_SECRET`, отдельные DB credentials, firewall и постоянный Docker volume. Не публикуйте PostgreSQL наружу без необходимости.
