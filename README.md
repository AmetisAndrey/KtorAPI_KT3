# Ktor Users API

Учебный REST API на Kotlin + Ktor: Swagger UI, OpenAPI, документация маршрутов,
автотесты, централизованные ошибки через StatusPages и middleware логирования.

## Требования
- JDK 17+
- Gradle 8.x (если Gradle Wrapper не создан локально)

## Запуск
```bash
gradle run
```
Сервер: http://localhost:8080

## Swagger / OpenAPI
- Swagger UI: http://localhost:8080/swagger
- OpenAPI YAML: http://localhost:8080/openapi.yaml

Swagger UI загружает спецификацию с `/openapi.yaml`. Для интерфейса требуется
доступ к CDN `unpkg.com`.

## Маршруты
| Метод | URL | Назначение |
|---|---|---|
| GET | `/api/health` | Проверка API |
| GET | `/api/users` | Список пользователей |
| POST | `/api/users` | Создание пользователя |
| GET | `/api/users/{id}` | Получение пользователя |
| DELETE | `/api/users/{id}` | Удаление пользователя |

Пример:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Alex","email":"alex@example.com"}'
```

## Тестирование
```bash
gradle test
```
Тесты проверяют health endpoint, создание/чтение, валидацию email, 404,
удаление, доступность Swagger и OpenAPI.

## StatusPages
Ошибки возвращаются в едином JSON-формате, например:
```json
{"error":"USER_NOT_FOUND","message":"User with id=999 was not found"}
```

## Middleware
`CallLogging` записывает HTTP-метод, URI и HTTP-статус. Конфигурация:
`src/main/kotlin/com/example/plugins/Monitoring.kt`.

## Структура
```text
src/main/kotlin/com/example/
  Application.kt
  models/User.kt
  repository/UserRepository.kt
  routes/Routing.kt
  plugins/Serialization.kt
  plugins/Monitoring.kt
  plugins/StatusPages.kt
src/main/resources/openapi.yaml
src/test/kotlin/com/example/ApplicationTest.kt
```

## Публикация в GitHub
Создай пустой репозиторий на GitHub, затем выполни в корне проекта:
```bash
git init
git add .
git commit -m "Initial Ktor Users API"
git branch -M main
git remote add origin https://github.com/ВАШ_ЛОГИН/ktor-users-api.git
git push -u origin main
```
Замени URL на адрес своего репозитория.

## Примечание
Хранилище пользователей работает в памяти процесса, поэтому данные сбрасываются
при перезапуске. Для production следует подключить базу данных.
