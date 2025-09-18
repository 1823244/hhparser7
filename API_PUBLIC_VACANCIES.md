# Публичный API для получения открытых вакансий

Этот API предоставляет доступ к открытым вакансиям без требования авторизации.

## Базовый URL
`/api/public/vacancies`

## Endpoints

### 1. Получить все открытые вакансии по проекту

**GET** `/api/public/vacancies/open/{projectId}`

**Параметры:**
- `projectId` (path) - ID проекта (обязательный)
- `reportDate` (query, опциональный) - дата отчета в формате YYYY-MM-DD (по умолчанию текущая дата)

**Пример запроса:**
```
GET /api/public/vacancies/open/1?reportDate=2025-09-10
```

**Ответ:**
```json
[
  {
    "id": 123,
    "name": "Java Developer",
    "hhid": "12345678",
    "salary_netto": 200000,
    "employer": "IT Company",
    "url": "https://hh.ru/vacancy/12345678",
    "employer_hhid": "87654321",
    "employer_link": "https://hh.ru/employer/87654321",
    "count": 1,
    "startDate": "2025-09-10",
    "regionName": "Москва"
  }
]
```

### 2. Получить новые вакансии за сегодня по проекту

**GET** `/api/public/vacancies/new/{projectId}`

**Параметры:**
- `projectId` (path) - ID проекта (обязательный)
- `reportDate` (query, опциональный) - дата отчета в формате YYYY-MM-DD (по умолчанию текущая дата)

**Пример запроса:**
```
GET /api/public/vacancies/new/1
```

**Ответ:** Аналогичен предыдущему endpoint

### 3. Получить открытые вакансии по проекту и поисковому тексту

**GET** `/api/public/vacancies/open/{projectId}/searchtext/{searchTextId}`

**Параметры:**
- `projectId` (path) - ID проекта (обязательный)
- `searchTextId` (path) - ID поискового текста (обязательный)

**Пример запроса:**
```
GET /api/public/vacancies/open/1/searchtext/5
```

**Ответ:** Аналогичен предыдущим endpoints

### 4. Получить детальное описание вакансии

**GET** `/api/public/vacancies/details/{hhid}`

**Параметры:**
- `hhid` (path) - ID вакансии на HeadHunter (обязательный)

**Пример запроса:**
```
GET /api/public/vacancies/details/12345678
```

**Ответ:**
```json
{
  "description": "<p>Мы ищем опытного Java разработчика...</p>",
  "alternate_url": "https://hh.ru/vacancy/12345678"
}
```

## Описание полей ответа

- `id` - внутренний ID вакансии в системе
- `name` - название вакансии
- `hhid` - ID вакансии на HeadHunter
- `salary_netto` - зарплата "на руки" (рассчитанная)
- `employer` - название работодателя
- `url` - ссылка на вакансию на HeadHunter
- `employer_hhid` - ID работодателя на HeadHunter
- `employer_link` - ссылка на профиль работодателя на HeadHunter
- `count` - порядковый номер в списке
- `startDate` - дата публикации/открытия вакансии
- `regionName` - название региона

### Поля для endpoint получения деталей вакансии

- `description` - HTML-описание вакансии
- `alternate_url` - ссылка на вакансию на HeadHunter

## Обработка ошибок

- `400 Bad Request` - неверный формат даты в параметре reportDate
- `404 Not Found` - проект или поисковый текст не найден
- `500 Internal Server Error` - внутренняя ошибка сервера

## Примечания

- API поддерживает CORS для всех источников
- Авторизация не требуется
- Все даты должны быть в формате ISO (YYYY-MM-DD)
- Зарплата указывается в рублях и рассчитывается как "на руки" (с учетом налогов)
