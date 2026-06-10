# Volunteer Platform

## О проекте
Платформа для публикации волонтёрских инициатив и подачи заявок на участие. 
Приложение предназначено для организации взаимодействия между пользователями в рамках волонтёрской активности.
Пользователь может зарегистрироваться, войти в систему, создать инициативу, управлять своими инициативами, 
просматривать профиль и откликаться на инициативы. Администратор дополнительно может управлять заявками и изменять их статус.

---

## Основные возможности
- регистрация и вход пользователя;
- JWT-аутентификация и защита маршрутов;
- просмотр и обновление профиля;
- создание, просмотр, редактирование и удаление инициатив;
- просмотр только своих инициатив;
- поиск инициатив по категории;
- создание заявок на участие;
- просмотр своих заявок;
- просмотр всех заявок для администратора;
- изменение статуса заявки;
- автоматическое повышение рейтинга пользователя после подтверждения заявки;
- пагинация для инициатив и заявок.

---

## Технологический стек

| Слой | Технологии |
|------|------------|
| Backend | Java 17, Spring Boot 3.0.13, Spring Web, Spring Data JPA, Spring Security, Hibernate Validator |
| Frontend | React, React Router DOM, Vite, JavaScript, CSS |
| База данных | H2, PostgreSQL |
| Миграции | Liquibase |
| Безопасность | JWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`) |
| Документация API | OpenAPI 3 (Swagger UI) |
| Сборка | Maven |
| Контейнеризация | Docker, Docker Compose |
| Тестирование | JUnit 5, Mockito, JaCoCo |

---

## Архитектура
Проект реализован по многослойной архитектуре, где каждая часть приложения отвечает за свою зону ответственности.

### Backend
#### `config`
Конфигурация приложения:
- `SecurityConfig` — настройка Spring Security, JWT, stateless session, обработка `401/403`;
- `WebConfig` — настройка CORS для frontend.

#### `controller`
REST-контроллеры для обработки HTTP-запросов.

#### `service`
Бизнес-логика:
- регистрация и авторизация;
- работа с инициативами;
- работа с заявками;
- пересчёт рейтинга и статуса пользователя.

#### `service.security`
Компоненты безопасности:
- `CustomUserDetailsService` — загрузка пользователя по логину;
- `JwtAuthenticationFilter` — фильтр JWT-аутентификации;
- `JwtService` — генерация и валидация JWT-токенов.

#### `repository`
Слой доступа к данным на основе Spring Data JPA.

#### `model`
JPA-сущности и перечисления (enums) предметной области.

#### `dto`
DTO-классы для входящих и исходящих данных API.

#### `exception`
Кастомные исключения и глобальный обработчик ошибок (`GlobalExceptionHandler`).

### Frontend
Frontend представляет собой SPA-приложение на React.
Основные части:
- `pages` — страницы интерфейса (главная, профиль, инициативы, детали инициативы);
- `components` — переиспользуемые компоненты (Header, Modal, ProtectedRoute, формы логина/регистрации);
- `api/api.js` — HTTP-клиент для взаимодействия с backend, автоматическая подстановка JWT-токена;
- `AuthContext` — контекст для хранения состояния авторизации;
- `ProtectedRoute` — компонент для защиты маршрутов от неавторизованного доступа.

---

## Структура проекта

```text
volunteer-platform/
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   │   └── api.js
│   │   ├── components/
│   │   │   ├── Header.jsx
│   │   │   ├── Modal.jsx
│   │   │   ├── LoginModal.jsx
│   │   │   ├── RegisterModal.jsx
│   │   │   └── ProtectedRoute.jsx
│   │   ├── context/
│   │   │   └── AuthContext.jsx
│   │   ├── pages/
│   │   │   ├── HomePage.jsx
│   │   │   ├── ProfilePage.jsx
│   │   │   ├── InitiativesPage.jsx
│   │   │   └── InitiativeDetailsPage.jsx
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   ├── package.json
│   ├── vite.config.js
│   └── index.html
│
├── src/
│   ├── main/
│   │   ├── java/ru/volunteer/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── VolunteerPlatformApplication.java
│   │   └── resources/
│   │       ├── db/changelog/
│   │       │   └── db.changelog-1.0-init-tables.xml
│   │       └── application.properties
│   └── test/
│
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── pom.xml
└── README.md
```

---
## Модель данных
### Основные сущности

| Сущность | Назначение |
|----------|------------|
| `User` | пользователь системы |
| `Role` | роль пользователя (`ROLE_USER`, `ROLE_ADMIN`) |
| `UserProfile` | дополнительная информация о пользователе (рейтинг, статус) |
| `Initiative` | волонтёрская инициатива |
| `Application` | заявка на участие в инициативе |

### Статусы заявки (ApplicationStatus)
- `PENDING` — на рассмотрении
- `APPROVED` — одобрена
- `REJECTED` — отклонена

### Статусы пользователя (UserStatus)
- `NO_PARTICIPATION` — без участия
- `BEGINNER` — начинающий
- `INTERMEDIATE` — опытный
- `ADVANCED` — эксперт

---

## Установка и запуск
### Требования
- Java 17 — запуск backend
- Maven — сборка backend
- Node.js и npm — запуск frontend
- Docker и Docker Compose (опционально, для контейнеризации)

### 1. Клонирование проекта
```bash
git clone <URL_репозитория>
cd volunteer-platform
```

### 2. Запуск backend
#### Локальный запуск (с H2)
```bash
mvn spring-boot:run
```

Backend будет доступен по адресу:
```text
http://localhost:8080
```

#### Запуск с PostgreSQL (через Docker)
```bash
docker run -d --name postgres -e POSTGRES_DB=volunteer_db -e POSTGRES_USER=volunteer_user -e POSTGRES_PASSWORD=volunteer_password -p 5432:5432 postgres:15-alpine
```

Затем в `application.properties` укажите:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/volunteer_db
spring.datasource.username=volunteer_user
spring.datasource.password=volunteer_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

### 3. Запуск frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend будет доступен по адресу:
```text
http://localhost:5173
```

### 4. Доступ к H2 Console (при использовании H2)
```text
http://localhost:8080/h2-console
```

Параметры подключения по умолчанию:
- JDBC URL: `jdbc:h2:mem:volunteerdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
- User Name: `volunteer_user`
- Password: пустой

### 5. Доступ к Swagger UI (документация API)
```text
http://localhost:8080/swagger-ui/index.html
```
---

## Docker-запуск
Проект можно запустить в Docker с использованием **PostgreSQL** и **Docker Compose**.
В этом режиме backend-приложение запускается в отдельном контейнере и подключается
к базе данных PostgreSQL в контейнере `postgres`.


### Запуск через Docker Compose
Из корня проекта выполните команду:

```bash
docker-compose up --build
```

Если нужен запуск в фоновом режиме:

```bash
docker-compose up --build -d
```

После запуска будут доступны:

- Backend API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- PostgreSQL: `localhost:5432`

### Остановка контейнеров

```bash
docker-compose down
```

Если нужно удалить контейнеры вместе с томом базы данных:

```bash
docker-compose down -v
```

### Как работает конфигурация
- контейнер `postgres` поднимает PostgreSQL 15 Alpine;
- контейнер `app` собирает и запускает Spring Boot приложение;
- приложение подключается к базе по адресу `jdbc:postgresql://postgres:5432/volunteer_db`;
- данные PostgreSQL сохраняются в Docker volume `postgres_data`, поэтому не теряются при перезапуске контейнеров;
- Liquibase автоматически применяет миграции при старте приложения.

---

## Конфигурация
Основные параметры находятся в файле:
```text
src/main/resources/application.properties
```

## Аутентификация и авторизация

Приложение использует JWT-аутентификацию. После успешного входа сервер
возвращает токен, который должен передаваться в заголовке `Authorization` для всех защищённых запросов.

```http
Authorization: Bearer <token>
```

### Роли
#### `ROLE_USER`
- регистрация и вход;
- работа с профилем;
- создание и просмотр инициатив;
- управление своими инициативами;
- создание и просмотр своих заявок.

#### `ROLE_ADMIN`
- все возможности `ROLE_USER`;
- просмотр всех заявок;
- изменение статусов заявок;
- удаление заявок.

---

## Тестирование
В проекте реализованы тесты для контроллеров и сервисов с использованием JUnit 5 и Mockito.

### Покрытие тестами (JaCoCo)
- Общее покрытие: **66%**
- Покрытие сервисного слоя: **92%**
- Количество тестов: **85+**

### Отчёт
```text
/target/site/jacoco/index.html
```

### Запуск тестов
```bash
mvn test
```
```