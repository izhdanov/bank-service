# Bank service (тестовое задание)

Java 11, Spring Boot 2.7, PostgreSQL, Flyway, Maven, JWT, Swagger, кэш Caffeine.

## Структура проекта

```
com.example.bank
  api/               REST-контроллеры, обработчик ошибок
  dto/               DTO запросов и ответов
  mapper/            преобразование сущностей в DTO
  service/           бизнес-логика
  dao/entity/        JPA-сущности
  dao/repository/    репозитории Spring Data и спецификации поиска
  security/, config/ JWT, Spring Security, Swagger
resources/db/migration/  V1__init.sql (схема БД)
resources/db/demo/       V1000__demo_users.sql (тестовые пользователи, только профиль "demo")
```

## Запуск

```
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Профиль `demo` дополнительно применяет миграцию Flyway `db/demo/V1000__demo_users.sql`, которая создаёт двух пользователей:

| Пользователь | Email | Телефон | Баланс |
|---|---|---|---|
| Alice Ivanova | alice@example.com | 79207865432 | 100.00 |
| Bob Petrov | bob@example.com | 79207865433 | 200.00 |

Пароль у обоих: `password123`. Хеш пароля считается в SQL через расширение `pgcrypto` (`crypt(..., gen_salt('bf'))`) и совместим с BCrypt из Spring Security. Без профиля `demo` создаётся только схема, пользователей нет.

Swagger UI: http://localhost:8080/swagger-ui.html

Переменные окружения: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (не менее 32 символов).

## Аутентификация

1. `POST /api/auth/login` с телом `{"login":"alice@example.com","password":"password123"}` возвращает токен. В токене единственный claim: `USER_ID`. В поле `login` можно передать email или телефон.
2. Дальше передавайте заголовок `Authorization: Bearer <token>`.

## API

| Метод и путь | Назначение |
|---|---|
| `GET /api/users?name=&email=&phone=&dateOfBirth=01.05.1993&page=0&size=20` | Поиск пользователей с пагинацией |
| `GET /api/users/{id}` | Карточка пользователя |
| `POST /api/users/me/emails`, `PUT` и `DELETE /api/users/me/emails/{id}` | Управление своими email |
| `POST /api/users/me/phones`, `PUT` и `DELETE /api/users/me/phones/{id}` | Управление своими телефонами |
| `GET /api/accounts/me` | Баланс текущего пользователя |
| `POST /api/transfers` с телом `{"toUserId":2,"value":10.50}` | Перевод денег |

Правила поиска:
- `dateOfBirth` (формат `дд.ММ.гггг`): пользователи, родившиеся позже указанной даты.
- `name`: поиск по префиксу (`LIKE 'текст%'`).
- `email` и `phone`: точное совпадение.
- `size` не больше 100.

Формат телефона: `79207865432` (11 цифр, начинается с 7). Занятый email или телефон даёт ответ 409. Удалить последний email или телефон нельзя.

## Особенности реализации

- **Перевод денег.** Одна транзакция. Оба счёта блокируются через `SELECT ... FOR UPDATE` в порядке возрастания id, поэтому нет потерянных обновлений и взаимных блокировок. Проверки: сумма положительная и не более двух знаков после запятой, нельзя перевести самому себе, получатель существует, на счёте хватает средств. В БД дополнительно стоит `CHECK (balance >= 0)`.
- **Начисление процентов.** Задача `@Scheduled` раз в 30 секунд выполняет один атомарный SQL `UPDATE`: баланс умножается на 1,10, но не превышает `initial_balance * 2.07`. При запуске нескольких экземпляров приложения нужна блокировка планировщика (например, ShedLock).
- **Кэш.** Кэш Caffeine `users` работает только на `UserService.getById` и сбрасывается при изменении email или телефонов. Результаты поиска и DAO-слой не кэшируются. Баланс не кэшируется.
- **Схема БД.** Таблица `USER` названа `users`, потому что `USER` зарезервировано в PostgreSQL. В `ACCOUNT` добавлено поле `INITIAL_BALANCE`, оно нужно для ограничения в 207%.
- **Ошибки.** Все ошибки возвращаются в едином формате `{"status", "message", "timestamp"}`.

## Тесты

`mvn test` запускает:
- `TransferServiceTest` - unit-тесты перевода на Mockito;
- `BankApiIT` - интеграционные тесты на Testcontainers и MockMvc (поиск, ответ 401, перевод, параллельные переводы). Нужен запущенный Docker.
