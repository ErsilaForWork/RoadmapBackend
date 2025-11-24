# RoadMap Backend — REST API для управления роадмапами

Backend-приложение на Spring Boot для создания и управления персональными роадмапами с целями и действиями.

## Что это и зачем

RoadMap Backend — это REST API, которое помогает пользователям создавать структурированные планы развития. Пользователь создает роадмап (например, "Изучить Java"), разбивает его на цели ("Изучить основы", "Изучить Spring"), а каждую цель — на конкретные действия ("Прочитать книгу", "Пройди курс"). Система автоматически отслеживает прогресс, переключает статусы задач и считает процент выполнения.

**Ключевые сценарии использования:**
- Создание персональных планов обучения или развития навыков
- Отслеживание прогресса выполнения задач с автоматическим расчетом процентов
- Управление последовательностью выполнения задач (можно перемещать цели и действия)
- Отслеживание активности пользователя (streak — серия дней активности)
- Автоматическое переключение статусов: когда завершаешь действие, система переключает на следующее

---

## Сущности / Модель данных

### `AppUser` (Пользователь)

- `userId` (Long, required) — уникальный идентификатор; пример: `1`
- `username` (String, required, unique) — имя пользователя для входа; пример: `"john_doe"`. Зачем: используется для аутентификации и отображения
- `email` (String, required, unique) — email для регистрации и уведомлений; пример: `"user@example.com"`
- `password` (String, required) — хешированный пароль; пример: `"$2a$10$..."`. Зачем: хранится в зашифрованном виде для безопасности
- `creationTime` (LocalDateTime, required) — дата и время регистрации; пример: `"2024-01-15T10:30:00"`, задается автоматический
- `role` (AppRole, required) — роль пользователя (USER/ADMIN); пример: `AppRole{roleId: 1, name: "ROLE_USER"}`. Зачем: определяет права доступа
- `lastActivityDate` (LocalDate, required) — дата последней активности; пример: `"2024-01-20"`. Зачем: для подсчета streak
- `streak` (Integer, required) — текущая серия дней активности; пример: `5`. Зачем: мотивация пользователя
- `streakBroken` (Boolean, required) — флаг, сломан ли streak; пример: `false`
- `lastLoginDate` (LocalDate, required) — дата последнего входа; пример: `"2024-01-20"`
- `verificationCode` (String, optional) — код для подтверждения email; пример: `"123456"`
- `verificationCodeExpires` (LocalDateTime, optional) — время истечения кода; пример: `"2024-01-15T11:00:00"`
- `enabled` (Boolean, required) — активирован ли аккаунт после верификации; пример: `true`

### `Roadmap` (Роадмап)

- `roadmapId` (Long, required) — уникальный идентификатор; пример: `1`
- `title` (String, required) — название роадмапа; пример: `"Изучить Java"`
- `completedPercent` (Integer, required) — процент выполнения (0-100); пример: `30`. Зачем: автоматически пересчитывается при завершении действий
- `status` (Status enum, required) — статус роадмапа: `NOT_COMPLETED`, `NOW_WORKING`, `COMPLETED`; пример: `NOW_WORKING`
- `nowWorkingGoal` (Goal, optional) — текущая активная цель; пример: `Goal{goalId: 2}`. Зачем: указывает, над какой целью сейчас работаем
- `goals` (List<Goal>, required) — список целей в роадмапе; пример: `[Goal{...}, Goal{...}]`
- `owner` (AppUser, required) — владелец роадмапа; пример: `AppUser{userId: 1}`. Зачем: определяет права доступа

### `Goal` (Цель)

- `goalId` (Long, required) — уникальный идентификатор; пример: `1`
- `title` (String, required) — название цели; пример: `"Изучить основы Java"`
- `completedPercent` (Integer, required) — процент выполнения (0-100); пример: `50`
- `status` (Status enum, required) — статус цели: `NOT_COMPLETED`, `NOW_WORKING`, `COMPLETED`; пример: `NOW_WORKING`
- `nowWorkingAction` (Action, optional) — текущее активное действие; пример: `Action{actionId: 3}`. Зачем: указывает, какое действие сейчас выполняется
- `roadmap` (Roadmap, required) — родительский роадмап; пример: `Roadmap{roadmapId: 1}`
- `actions` (List<Action>, required) — список действий в цели; пример: `[Action{...}, Action{...}]`
- `position` (Long, optional) — позиция в списке целей; пример: `1000`. Зачем: для сортировки и перемещения целей

### `Action` (Действие)

- `actionId` (Long, required) — уникальный идентификатор; пример: `1`
- `title` (String, required) — название действия; пример: `"Прочитать книгу 'Java: The Complete Reference'"`
- `description` (String, optional) — описание действия; пример: `"Изучить главы 1-5"`
- `status` (Status enum, required) — статус действия: `NOT_COMPLETED`, `NOW_WORKING`, `COMPLETED`; пример: `NOT_COMPLETED`
- `goal` (Goal, required) — родительская цель; пример: `Goal{goalId: 1}`
- `position` (Long, optional) — позиция в списке действий; пример: `500`. Зачем: для сортировки и перемещения действий

### `AppRole` (Роль)

- `roleId` (Long, required) — уникальный идентификатор; пример: `1`
- `name` (String, required) — название роли; пример: `"ROLE_USER"` или `"ROLE_ADMIN"`

### Связи между сущностями

- `AppUser 1 — N Roadmap` (через поле `owner` в Roadmap, внешний ключ `owner_id`)
- `Roadmap 1 — N Goal` (через поле `roadmap` в Goal, внешний ключ `roadmap_id`)
- `Roadmap 1 — 1 Goal` (через поле `nowWorkingGoal` в Roadmap, внешний ключ `working_id`)
- `Goal 1 — N Action` (через поле `goal` в Action, внешний ключ `goal_id`)
- `Goal 1 — 1 Action` (через поле `nowWorkingAction` в Goal, внешний ключ `working_id`)
- `AppUser N — 1 AppRole` (через поле `role` в AppUser, внешний ключ `role_id`)

---

## API / Эндпоинты

### Аутентификация и регистрация

#### Регистрация пользователя

**Описание:** Создает нового пользователя и отправляет код верификации на email.

**Метод + Путь:** `POST /security/public/register`

**Авторизация:** Не требуется

**Вход (request):**
```json
{
  "username": "john_doe",
  "email": "user@example.com",
  "password": "password123"
}
```

**Обязательные поля:**
- `username` — строка, только латинские буквы, цифры и подчеркивание, максимум 256 символов
- `email` — валидный email адрес
- `password` — минимум 8 символов, максимум 256

**Ответы (responses):**

- **201 Created** — пользователь создан, код отправлен:
```json
{
  "message": "Verification code is sent, if email exists"
}
```

- **400 Bad Request** — ошибка валидации:
```json
{
  "message": "Incorrect registration form"
}
```
Причины: пустые обязательные поля, невалидный email, пароль меньше 8 символов, username содержит недопустимые символы.

- **409 Conflict** — пользователь уже существует:
```json
{
  "message": "User with this username already exists"
}
```
или
```json
{
  "message": "User with this email already exists"
}
```

**Примеры:**

```bash
curl -X POST http://localhost:8080/security/public/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Замечания:** Код верификации отправляется асинхронно. Если email уже существует, ответ все равно будет 201 (безопасность).

---

#### Подтверждение email

**Описание:** Подтверждает email пользователя кодом верификации и возвращает JWT токен в cookie.

**Метод + Путь:** `POST /security/public/verify`

**Авторизация:** Не требуется

**Вход (request):**
```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

**Обязательные поля:**
- `email` — email пользователя
- `verificationCode` — код из письма

**Ответы (responses):**

- **200 OK** — пользователь подтвержден, JWT установлен в cookie `jwt`:
```json
{
  "message": "User verified successfully"
}
```
В заголовке `Set-Cookie` устанавливается JWT токен.

- **400 Bad Request** — неверный код или email:
```json
{
  "message": "Invalid verification code"
}
```
или
```json
{
  "message": "Verification code expired"
}
```
или
```json
{
  "message": "User not found"
}
```

**Примеры:**

```bash
curl -X POST http://localhost:8080/security/public/verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "verificationCode": "123456"
  }' \
  -c cookies.txt
```

**Замечания:** JWT токен устанавливается в HTTP-only cookie. Срок действия токена: 84000000 миллисекунд (примерно 23 часа).

---

#### Повторная отправка кода верификации

**Описание:** Отправляет новый код верификации на email.

**Метод + Путь:** `POST /security/public/verification-code`

**Авторизация:** Не требуется

**Вход (request):**
```json
{
  "email": "user@example.com"
}
```

**Обязательные поля:**
- `email` — email пользователя

**Ответы (responses):**

- **200 OK** — код отправлен:
```json
{
  "message": "Verification code sent!"
}
```

- **400 Bad Request** — пользователь не найден или уже верифицирован:
```json
{
  "message": "User not found"
}
```
или
```json
{
  "message": "User already verified"
}
```

**Примеры:**

```bash
curl -X POST http://localhost:8080/security/public/verification-code \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com"
  }'
```

---

#### Вход в систему

**Описание:** Аутентифицирует пользователя и возвращает JWT токен в cookie.

**Метод + Путь:** `POST /security/public/login`

**Авторизация:** Не требуется

**Вход (request):**
```json
{
  "username": "john_doe",
  "password": "password123"
}
```

**Обязательные поля:**
- `username` — имя пользователя
- `password` — пароль

**Ответы (responses):**

- **200 OK** — вход выполнен, JWT установлен в cookie:
```json
{
  "message": "Logged in successfully"
}
```

- **400 Bad Request** — ошибка валидации:
```json
{
  "message": "Incorrect login form"
}
```

- **401 Unauthorized** — неверные учетные данные:
```json
{
  "message": "Invalid username or password"
}
```

**Примеры:**

```bash
curl -X POST http://localhost:8080/security/public/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }' \
  -c cookies.txt
```

---

#### Выход из системы

**Описание:** Удаляет JWT cookie и завершает сессию.

**Метод + Путь:** `POST /security/public/logout`

**Авторизация:** Не требуется

**Вход (request):** Тело запроса не требуется

**Ответы (responses):**

- **200 OK** — выход выполнен:
```json
{
  "message": "Logged out successfully"
}
```
Cookie `jwt` удаляется через заголовок `Set-Cookie` с `maxAge=0`.

**Примеры:**

```bash
curl -X POST http://localhost:8080/security/public/logout \
  -b cookies.txt
```

---

#### Получение данных текущего пользователя

**Описание:** Возвращает информацию о текущем аутентифицированном пользователе.

**Метод + Путь:** `GET /security/data`

**Авторизация:** Требуется (JWT в cookie)

**Вход (request):** Тело запроса не требуется

**Ответы (responses):**

- **200 OK** — данные пользователя:
```json
{
  "userId": 1,
  "username": "john_doe",
  "email": "user@example.com",
  "phoneNumber": "+79991234567",
  "creationTime": "2024-01-15T10:30:00",
  "role": {
    "roleId": 1,
    "name": "ROLE_USER"
  },
  "roadmaps": [],
  "streak": 5,
  "streakBroken": false
}
```

- **400 Bad Request** — пользователь не найден:
```json
{
  "message": "User not found"
}
```

- **401 Unauthorized** — не авторизован (нет токена или токен невалидный)

**Примеры:**

```bash
curl -X GET http://localhost:8080/security/data \
  -b cookies.txt
```

---

#### Получение всех пользователей

**Описание:** Возвращает список всех пользователей (только для администраторов).

**Метод + Путь:** `GET /security/users`

**Авторизация:** Требуется (JWT в cookie), роль ADMIN

**Вход (request):** Тело запроса не требуется

**Ответы (responses):**

- **200 OK** — список пользователей:
```json
[
  {
    "userId": 1,
    "username": "john_doe",
    "email": "user@example.com",
    "phoneNumber": "+79991234567",
    "creationTime": "2024-01-15T10:30:00",
    "role": {
      "roleId": 1,
      "name": "ROLE_USER"
    },
    "roadmaps": [],
    "streak": 5,
    "streakBroken": false
  }
]
```

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав (не ADMIN)

**Примеры:**

```bash
curl -X GET http://localhost:8080/security/users \
  -b cookies.txt
```

---

### Роадмапы

#### Создание роадмапа

**Описание:** Создает новый роадмап с целями и действиями для текущего пользователя.

**Метод + Путь:** `POST /roadmap`

**Авторизация:** Требуется (JWT в cookie)

**Вход (request):**
```json
{
  "title": "Изучить Java",
  "goals": [
    {
      "title": "Изучить основы Java",
      "actions": [
        {
          "title": "Прочитать книгу 'Java: The Complete Reference'",
          "description": "Изучить главы 1-5"
        },
        {
          "title": "Пройди курс на Coursera",
          "description": "Java Programming and Software Engineering Fundamentals"
        }
      ]
    },
    {
      "title": "Изучить Spring Framework",
      "actions": [
        {
          "title": "Изучить Spring Core",
          "description": "Dependency Injection, IoC Container"
        }
      ]
    }
  ]
}
```

**Обязательные поля:**
- `title` — название роадмапа (не пустое)
- `goals` — массив целей (может быть пустым)
  - `title` — название цели (не пустое)
  - `actions` — массив действий (может быть пустым)
    - `title` — название действия (не пустое)
    - `description` — описание (опционально)

**Ответы (responses):**

- **200 OK** — роадмап создан:
```json
{
  "id": 1,
  "title": "Изучить Java",
  "completedPercent": 0,
  "status": "NOT_COMPLETED",
  "owner": {
    "userId": 1,
    "username": "john_doe",
    "role": {
      "roleId": 1,
      "name": "ROLE_USER"
    }
  },
  "goals": [
    {
      "goalId": 1,
      "title": "Изучить основы Java",
      "completedPercent": 0,
      "status": "NOT_COMPLETED",
      "position": 1000,
      "actions": [
        {
          "actionId": 1,
          "title": "Прочитать книгу 'Java: The Complete Reference'",
          "description": "Изучить главы 1-5",
          "status": "NOT_COMPLETED",
          "position": 500
        }
      ]
    }
  ]
}
```

- **400 Bad Request** — ошибка валидации:
```json
{
  "message": "Invalid input data"
}
```
или
```json
{
  "message": "No such user!"
}
```

- **401 Unauthorized** — не авторизован:
```json
{
  "message": "Unauthorized"
}
```

**Примеры:**

```bash
curl -X POST http://localhost:8080/roadmap \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Изучить Java",
    "goals": [
      {
        "title": "Изучить основы Java",
        "actions": [
          {
            "title": "Прочитать книгу",
            "description": "Изучить главы 1-5"
          }
        ]
      }
    ]
  }'
```

---

#### Получение всех роадмапов текущего пользователя

**Описание:** Возвращает список всех роадмапов текущего аутентифицированного пользователя.

**Метод + Путь:** `GET /user/roadmaps`

**Авторизация:** Требуется (JWT в cookie)

**Вход (request):** Тело запроса не требуется

**Ответы (responses):**

- **200 OK** — список роадмапов:
```json
[
  {
    "id": 1,
    "title": "Изучить Java",
    "completedPercent": 30,
    "status": "NOW_WORKING",
    "owner": {
      "userId": 1,
      "username": "john_doe",
      "role": {
        "roleId": 1,
        "name": "ROLE_USER"
      }
    },
    "goals": []
  }
]
```

- **401 Unauthorized** — не авторизован

**Примеры:**

```bash
curl -X GET http://localhost:8080/user/roadmaps \
  -b cookies.txt
```

---

#### Получение завершенных роадмапов пользователя

**Описание:** Возвращает список завершенных роадмапов текущего пользователя.

**Метод + Путь:** `GET /user/roadmaps/completed`

**Авторизация:** Требуется (JWT в cookie)

**Вход (request):** Тело запроса не требуется

**Ответы (responses):**

- **200 OK** — список завершенных роадмапов (формат как в `GET /user/roadmaps`, но только со статусом `COMPLETED`)

- **401 Unauthorized** — не авторизован

**Примеры:**

```bash
curl -X GET http://localhost:8080/user/roadmaps/completed \
  -b cookies.txt
```

---

#### Получение незавершенных роадмапов пользователя

**Описание:** Возвращает список незавершенных роадмапов текущего пользователя.

**Метод + Путь:** `GET /user/roadmaps/notcompleted`

**Авторизация:** Требуется (JWT в cookie)

**Вход (request):** Тело запроса не требуется

**Ответы (responses):**

- **200 OK** — список незавершенных роадмапов (формат как в `GET /user/roadmaps`, но только со статусом `NOT_COMPLETED` или `NOW_WORKING`)

- **401 Unauthorized** — не авторизован

**Примеры:**

```bash
curl -X GET http://localhost:8080/user/roadmaps/notcompleted \
  -b cookies.txt
```

---

#### Получение всех роадмапов

**Описание:** Возвращает список всех роадмапов в системе (для всех пользователей).

**Метод + Путь:** `GET /roadmaps`

**Авторизация:** Не требуется (публичный эндпоинт)

**Вход (request):** Тело запроса не требуется

**Ответы (responses):**

- **200 OK** — список всех роадмапов (формат как в `GET /user/roadmaps`)

**Примеры:**

```bash
curl -X GET http://localhost:8080/roadmaps
```

---

#### Обновление роадмапа

**Описание:** Частично обновляет роадмап (можно изменить только название).

**Метод + Путь:** `PATCH /roadmap/{id}`

**Авторизация:** Требуется (JWT в cookie), владелец роадмапа или ADMIN

**Вход (request):**
```json
{
  "title": "Новое название роадмапа"
}
```

**Обязательные поля:**
- `title` — новое название (максимум 2000 символов)

**Ответы (responses):**

- **200 OK** — роадмап обновлен (тело ответа пустое)

- **400 Bad Request** — ошибка валидации:
```json
{
  "message": "Bad title"
}
```
Причины: название слишком длинное (больше 2000 символов), пустое поле.

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав (не владелец и не ADMIN)
- **404 Not Found** — роадмап не найден

**Примеры:**

```bash
curl -X PATCH http://localhost:8080/roadmap/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Новое название роадмапа"
  }'
```

---

#### Удаление роадмапа

**Описание:** Удаляет роадмап и все связанные цели и действия.

**Метод + Путь:** `DELETE /roadmap/{id}`

**Авторизация:** Требуется (JWT в cookie), владелец роадмапа или ADMIN

**Вход (request):** Тело запроса не требуется

**Параметры пути:**
- `id` — ID роадмапа

**Ответы (responses):**

- **200 OK** — роадмап удален:
```json
{
  "message": "Succesfully deleted"
}
```

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав (не владелец и не ADMIN)
- **404 Not Found** — роадмап не найден

**Примеры:**

```bash
curl -X DELETE http://localhost:8080/roadmap/1 \
  -b cookies.txt
```

**Замечания:** Удаление каскадное — удаляются все цели и действия роадмапа.

---

### Цели

#### Удаление цели

**Описание:** Удаляет цель и все связанные действия. Автоматически переключает статусы: если удаляемая цель была активной (`NOW_WORKING`), система переключает на следующую цель.

**Метод + Путь:** `DELETE /goal/{id}`

**Авторизация:** Требуется (JWT в cookie), владелец цели или ADMIN

**Вход (request):** Тело запроса не требуется

**Параметры пути:**
- `id` — ID цели

**Ответы (responses):**

- **200 OK** — цель удалена:
```json
{
  "message": "Successfully deleted!"
}
```

- **400 Bad Request** — ошибка при удалении:
```json
{
  "message": "Cant delete the completed goal!"
}
```
или другие сообщения об ошибках.

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав (не владелец и не ADMIN)
- **404 Not Found** — цель не найдена

**Примеры:**

```bash
curl -X DELETE http://localhost:8080/goal/1 \
  -b cookies.txt
```

**Замечания:** 
- Нельзя удалить завершенную цель (`status = COMPLETED`).
- Если удаляется последняя цель в роадмапе и она была активной, роадмап помечается как завершенный.

---

#### Обновление цели

**Описание:** Частично обновляет цель (можно изменить только название).

**Метод + Путь:** `PATCH /goal/{id}`

**Авторизация:** Требуется (JWT в cookie), владелец цели или ADMIN

**Вход (request):**
```json
{
  "title": "Новое название цели"
}
```

**Обязательные поля:**
- `title` — новое название (максимум 2000 символов)

**Ответы (responses):**

- **200 OK** — цель обновлена (тело ответа пустое)

- **400 Bad Request** — ошибка валидации или ограничений:
```json
{
  "message": "Bad request body"
}
```
или
```json
{
  "message": "Unable to save the entity"
}
```

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав
- **404 Not Found** — цель не найдена

**Примеры:**

```bash
curl -X PATCH http://localhost:8080/goal/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Новое название цели"
  }'
```

---

#### Перемещение цели

**Описание:** Перемещает цель в другую позицию в списке целей роадмапа. Используется для изменения порядка выполнения.

**Метод + Путь:** `PATCH /goal/move/{id}`

**Авторизация:** Требуется (JWT в cookie), владелец цели или ADMIN

**Вход (request):**
```json
{
  "prevId": 2,
  "nextId": 4
}
```

**Параметры:**
- `prevId` (Long, optional) — ID предыдущей цели (если перемещаем в начало, можно не указывать)
- `nextId` (Long, optional) — ID следующей цели (если перемещаем в конец, можно не указывать)
- Оба параметра не могут быть `null` одновременно

**Ответы (responses):**

- **200 OK** — цель перемещена (тело ответа пустое)

- **400 Bad Request** — ошибка валидации:
```json
{
  "message": "No such action with id: 123"
}
```
или
```json
{
  "message": "Unable to move completed action!"
}
```
или
```json
{
  "message": "Both previous and next ids are empty!"
}
```

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав
- **404 Not Found** — цель не найдена

**Примеры:**

```bash
curl -X PATCH http://localhost:8080/goal/move/3 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "prevId": 2,
    "nextId": 4
  }'
```

**Замечания:** 
- Нельзя перемещать завершенные цели.
- Нельзя перемещать цель в блок завершенных задач (между двумя завершенными целями).

---

### Действия

#### Завершение действия

**Описание:** Помечает действие как завершенное и автоматически переключает статусы: если это было последнее действие в цели, цель завершается; если это была последняя цель в роадмапе, роадмап завершается. Также переключает на следующее действие/цель, если есть.

**Метод + Путь:** `PUT /action/complete/{id}`

**Авторизация:** Требуется (JWT в cookie), владелец действия или ADMIN

**Вход (request):** Тело запроса не требуется

**Параметры пути:**
- `id` — ID действия

**Ответы (responses):**

- **200 OK** — действие завершено, возвращается обновленный роадмап:
```json
{
  "id": 1,
  "title": "Изучить Java",
  "completedPercent": 35,
  "status": "NOW_WORKING",
  "owner": {
    "userId": 1,
    "username": "john_doe",
    "role": {
      "roleId": 1,
      "name": "ROLE_USER"
    }
  },
  "goals": [
    {
      "goalId": 1,
      "title": "Изучить основы Java",
      "completedPercent": 50,
      "status": "NOW_WORKING",
      "position": 1000,
      "actions": [
        {
          "actionId": 1,
          "title": "Прочитать книгу",
          "description": "Изучить главы 1-5",
          "status": "COMPLETED",
          "position": 500
        },
        {
          "actionId": 2,
          "title": "Пройди курс",
          "status": "NOW_WORKING",
          "position": 1000
        }
      ]
    }
  ]
}
```

- **400 Bad Request** — ошибка:
```json
{
  "message": "No such action id"
}
```
или
```json
{
  "message": "Action id cant be completed"
}
```
Причина последней ошибки: действие не является текущим активным действием (`nowWorkingAction`) в своей цели.

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав
- **404 Not Found** — действие не найдено

**Примеры:**

```bash
curl -X PUT http://localhost:8080/action/complete/1 \
  -b cookies.txt
```

**Замечания:** 
- Можно завершить только текущее активное действие (`status = NOW_WORKING` и оно должно быть `nowWorkingAction` в своей цели).
- При завершении действия автоматически пересчитывается процент выполнения цели и роадмапа.
- Если завершается последнее действие в последней цели, роадмап помечается как завершенный.

---

#### Удаление действия

**Описание:** Удаляет действие. Автоматически переключает статусы, если удаляемое действие было активным.

**Метод + Путь:** `DELETE /action/{id}`

**Авторизация:** Требуется (JWT в cookie), владелец действия или ADMIN

**Вход (request):** Тело запроса не требуется

**Параметры пути:**
- `id` — ID действия

**Ответы (responses):**

- **200 OK** — действие удалено:
```json
{
  "message": "Successfully deleted!"
}
```

- **400 Bad Request** — ошибка при удалении:
```json
{
  "message": "Cant delete the completed action!"
}
```
или другие сообщения об ошибках.

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав
- **404 Not Found** — действие не найдено

**Примеры:**

```bash
curl -X DELETE http://localhost:8080/action/1 \
  -b cookies.txt
```

**Замечания:** 
- Нельзя удалить завершенное действие (`status = COMPLETED`).
- Если удаляется активное действие (`nowWorkingAction`), система переключает на следующее действие.

---

#### Обновление действия

**Описание:** Частично обновляет действие (можно изменить название и описание).

**Метод + Путь:** `PATCH /action/{id}`

**Авторизация:** Требуется (JWT в cookie), владелец действия или ADMIN

**Вход (request):**
```json
{
  "title": "Новое название действия",
  "description": "Новое описание"
}
```

**Обязательные поля:**
- `title` — новое название (максимум 2000 символов, опционально)
- `description` — новое описание (максимум 3000 символов, опционально)

**Ответы (responses):**

- **200 OK** — действие обновлено (тело ответа пустое)

- **400 Bad Request** — ошибка валидации:
```json
{
  "message": "Bad request body!"
}
```
или
```json
{
  "message": "No such action with id: 123"
}
```

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав
- **404 Not Found** — действие не найдено

**Примеры:**

```bash
curl -X PATCH http://localhost:8080/action/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Новое название действия",
    "description": "Новое описание"
  }'
```

---

#### Перемещение действия

**Описание:** Перемещает действие в другую позицию в списке действий цели. Используется для изменения порядка выполнения.

**Метод + Путь:** `PATCH /action/move/{id}`

**Авторизация:** Требуется (JWT в cookie), владелец действия или ADMIN

**Вход (request):**
```json
{
  "prevId": 2,
  "nextId": 4
}
```

**Параметры:**
- `prevId` (Long, optional) — ID предыдущего действия (если перемещаем в начало, можно не указывать)
- `nextId` (Long, optional) — ID следующего действия (если перемещаем в конец, можно не указывать)
- Оба параметра не могут быть `null` одновременно

**Ответы (responses):**

- **200 OK** — действие перемещено (тело ответа пустое)

- **400 Bad Request** — ошибка валидации:
```json
{
  "message": "No such action with id: 123"
}
```
или
```json
{
  "message": "Unable to move completed action!"
}
```
или
```json
{
  "message": "Both previous and next ids are empty!"
}
```

- **401 Unauthorized** — не авторизован
- **403 Forbidden** — недостаточно прав
- **404 Not Found** — действие не найдено

**Примеры:**

```bash
curl -X PATCH http://localhost:8080/action/move/3 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "prevId": 2,
    "nextId": 4
  }'
```

**Замечания:** 
- Нельзя перемещать завершенные действия.
- Нельзя перемещать действие в блок завершенных задач (между двумя завершенными действиями).

---

## Как запустить локально

### Требования

- Java 21 или выше
- Maven 3.6+
- PostgreSQL 12+
- Настроенный SMTP сервер для отправки email (опционально, для регистрации)

### Переменные окружения

Создайте файл `.env` в корне проекта со следующим содержимым:

```properties
# База данных
DB_PASSWORD=your_postgres_password

# JWT
JWT_SECRET=your_secret_key_min_32_characters_long

# Email (для отправки кодов верификации)
SENDING_EMAIL=your_email@gmail.com
EMAIL_PASSWORD=your_app_password
```

**Объяснение переменных:**
- `DB_PASSWORD` — пароль для подключения к PostgreSQL
- `JWT_SECRET` — секретный ключ для подписи JWT токенов (минимум 32 символа)
- `SENDING_EMAIL` — email адрес, с которого будут отправляться письма
- `EMAIL_PASSWORD` — пароль приложения для email (для Gmail используйте App Password)

### Настройка базы данных

1. Создайте базу данных PostgreSQL:
```sql
CREATE DATABASE "RoadMap";
```

2. Приложение автоматически создаст таблицы при первом запуске (используется `spring.jpa.hibernate.ddl-auto=update`).

### Запуск приложения

1. Установите зависимости:
```bash
mvn clean install
```

2. Запустите приложение:
```bash
mvn spring-boot:run
```

Или через IDE (запустите класс `RoadMapBackendApplication`).

Приложение запустится на `http://localhost:8080` (порт по умолчанию).

### Миграции

Миграции выполняются автоматически при запуске через Hibernate (`ddl-auto=update`). Для продакшена рекомендуется использовать Flyway или Liquibase.

---

## Как тестировать эндпоинты

### Использование curl

Все примеры curl команд приведены в разделе "API / Эндпоинты" выше. Для работы с аутентификацией сохраняйте cookies в файл:

```bash
# Регистрация и верификация
curl -X POST http://localhost:8080/security/public/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}' \
  -c cookies.txt

# Вход
curl -X POST http://localhost:8080/security/public/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}' \
  -c cookies.txt

# Использование cookies для авторизованных запросов
curl -X GET http://localhost:8080/user/roadmaps \
  -b cookies.txt
```

### Использование Postman

1. **Создайте коллекцию** с эндпоинтами из раздела "API / Эндпоинты".

2. **Настройте переменные окружения:**
   - `base_url` = `http://localhost:8080`

3. **Для авторизованных запросов:**
   - После логина или верификации JWT токен сохраняется в cookie автоматически.
   - В Postman включите опцию "Send cookies" в настройках запроса.

4. **Пример последовательности:**
   - `POST /security/public/register` → получить код верификации
   - `POST /security/public/verify` → подтвердить email, получить JWT
   - `POST /roadmap` → создать роадмап
   - `GET /user/roadmaps` → получить список роадмапов
   - `PUT /action/complete/{id}` → завершить действие

### Использование Insomnia

Аналогично Postman. Убедитесь, что включена опция автоматического использования cookies.

---

## Дополнительная информация

### Статусы (Status enum)

- `NOT_COMPLETED` — не начато / не завершено
- `NOW_WORKING` — в процессе выполнения (текущая активная задача)
- `COMPLETED` — завершено

### Автоматическое переключение статусов

Система автоматически управляет статусами:

1. При завершении действия (`PUT /action/complete/{id}`):
   - Действие помечается как `COMPLETED`
   - Если это было последнее действие в цели → цель помечается как `COMPLETED`
   - Если это была последняя цель в роадмапе → роадмап помечается как `COMPLETED`
   - Иначе → следующее действие становится `NOW_WORKING`

2. При удалении активной цели/действия:
   - Система переключает на следующую цель/действие

### Расчет процентов выполнения

- Процент выполнения цели = (количество завершенных действий / общее количество действий) × 100
- Процент выполнения роадмапа = среднее арифметическое процентов выполнения всех целей

### Авторизация

- JWT токен передается через HTTP-only cookie (`jwt`)
- Срок действия токена: 84000000 миллисекунд (≈23 часа)
- Для доступа к защищенным эндпоинтам требуется валидный токен
- Владелец ресурса или пользователь с ролью `ROLE_ADMIN` может изменять/удалять ресурсы

---

## Примечания

- Все даты и время в формате ISO 8601 (например, `2024-01-15T10:30:00`)
- Максимальная длина названий: 2000 символов для роадмапов и целей, 2000 для действий
- Максимальная длина описания действия: 3000 символов
- При удалении роадмапа каскадно удаляются все цели и действия
- При удалении цели каскадно удаляются все действия

