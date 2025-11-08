# 📍 API de Reservas - Documentación

## Base URL
```
http://localhost:4002
```

---

## 🏢 Endpoints de Locations

### 1. Obtener todas las ubicaciones
```http
GET /locations
```

**Respuesta exitosa (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Norte",
    "capacity": 10,
    "address": "Avenida Norte 456"
  },
  {
    "id": 2,
    "name": "Sur",
    "capacity": 10,
    "address": "Calle Principal 123"
  }
]
```

---

### 2. Obtener una ubicación específica
```http
GET /locations/{id}
```

**Ejemplo:**
```http
GET /locations/1
```

**Respuesta exitosa (200 OK):**
```json
{
  "id": 1,
  "name": "Norte",
  "capacity": 10,
  "address": "Avenida Norte 456"
}
```

---

### 3. Consultar disponibilidad
```http
GET /locations/{id}/availability?mealTime={MEAL_TIME}&date={DATE}
```

**Parámetros de Query:**
- `mealTime` (requerido): `DESAYUNO`, `ALMUERZO`, `MERIENDA`, `CENA`
- `date` (requerido): Formato ISO 8601: `2025-11-02T00:00:00`

**Ejemplo en Postman:**
```
GET http://localhost:4002/locations/1/availability?mealTime=ALMUERZO&date=2025-11-02T00:00:00
```

**Respuesta exitosa (200 OK):**
```json
[
  {
    "timeSlot": "ALMUERZO_SLOT_1",
    "availableSeats": 7,
    "totalCapacity": 10,
    "available": true
  },
  {
    "timeSlot": "ALMUERZO_SLOT_2",
    "availableSeats": 10,
    "totalCapacity": 10,
    "available": true
  }
]
```

---

## 📅 Endpoints de Reservas

### 4. Crear una reserva
```http
POST /reservations
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "userId": 1,
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationTimeSlot": "ALMUERZO_SLOT_1",
  "reservationDate": "2025-11-02T00:00:00"
}
```

**Campos obligatorios:**
- `userId` (Long): ID del usuario que hace la reserva
- `locationId` (Long): ID de la ubicación (1=Norte, 2=Sur)
- `mealTime` (String): `DESAYUNO`, `ALMUERZO`, `MERIENDA`, `CENA`
- `reservationTimeSlot` (String): Debe corresponder al mealTime:
  - **DESAYUNO**: `DESAYUNO_SLOT_1` (7-8h), `DESAYUNO_SLOT_2` (8-9h)
  - **ALMUERZO**: `ALMUERZO_SLOT_1` (12-13h), `ALMUERZO_SLOT_2` (13-14h)
  - **MERIENDA**: `MERIENDA_SLOT_1` (16-17h), `MERIENDA_SLOT_2` (17-18h)
  - **CENA**: `CENA_SLOT_1` (20-21h), `CENA_SLOT_2` (21-22h)
- `reservationDate` (String): Fecha en formato ISO 8601: `YYYY-MM-DDTHH:mm:ss`

**Respuesta exitosa (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationTimeSlot": "ALMUERZO_SLOT_1",
  "reservationDate": "2025-11-02T00:00:00",
  "status": "ACTIVA",
  "cost": 25.0,
  "createdAt": "2025-11-01T20:23:18.421674"
}
```

**Respuesta de error - Time slot no válido (400 BAD REQUEST):**
```json
{
  "timestamp": "2025-11-01T20:25:00",
  "status": 400,
  "error": "400 BAD_REQUEST",
  "message": "El time slot no corresponde al meal time especificado"
}
```

**Respuesta de error - Sin capacidad (409 CONFLICT):**
```json
{
  "timestamp": "2025-11-01T20:29:38.680518",
  "status": 409,
  "error": "409 CONFLICT",
  "message": "No hay capacidad disponible para ALMUERZO_SLOT_1 en Norte el 2025-11-02. Capacidad máxima: 10"
}
```

---

### 5. Obtener todas las reservas
```http
GET /reservations
```

**Respuesta exitosa (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "locationId": 1,
    "mealTime": "ALMUERZO",
    "reservationTimeSlot": "ALMUERZO_SLOT_1",
    "reservationDate": "2025-11-02T00:00:00",
    "status": "ACTIVA",
    "cost": 25.0,
    "createdAt": "2025-11-01T20:23:18.421674"
  },
  {
    "id": 2,
    "userId": 2,
    "locationId": 1,
    "mealTime": "ALMUERZO",
    "reservationTimeSlot": "ALMUERZO_SLOT_1",
    "reservationDate": "2025-11-02T00:00:00",
    "status": "ACTIVA",
    "cost": 25.0,
    "createdAt": "2025-11-01T20:23:47.275306"
  }
]
```

---

### 6. Obtener reservas de un usuario
```http
GET /reservations/mine?userId={USER_ID}
```

**Ejemplo:**
```
GET /reservations/mine?userId=1
```

**Respuesta exitosa (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "locationId": 1,
    "mealTime": "ALMUERZO",
    "reservationTimeSlot": "ALMUERZO_SLOT_1",
    "reservationDate": "2025-11-02T00:00:00",
    "status": "ACTIVA",
    "cost": 25.0,
    "createdAt": "2025-11-01T20:23:18.421674"
  }
]
```

---

### 7. Obtener una reserva por ID
```http
GET /reservations/byreservationId/{reservationId}
```

**Ejemplo:**
```
GET /reservations/byreservationId/1
```

**Respuesta exitosa (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationTimeSlot": "ALMUERZO_SLOT_1",
  "reservationDate": "2025-11-02T00:00:00",
  "status": "ACTIVA",
  "cost": 25.0,
  "createdAt": "2025-11-01T20:23:18.421674"
}
```

---

## 📝 Guía paso a paso para Postman

### Crear una reserva en Postman:

1. **Abrí Postman** y creá una nueva request

2. **Configurá el método y URL:**
   - Método: `POST`
   - URL: `http://localhost:4002/reservations`

3. **Configurá los Headers:**
   - Click en la pestaña "Headers"
   - Agregá: `Content-Type: application/json`

4. **Configurá el Body:**
   - Click en la pestaña "Body"
   - Seleccioná "raw"
   - Asegurate que el dropdown de la derecha diga "JSON"
   - Pegá este JSON:
   ```json
   {
     "userId": 1,
     "locationId": 1,
     "mealTime": "ALMUERZO",
     "reservationTimeSlot": "ALMUERZO_SLOT_1",
     "reservationDate": "2025-11-02T00:00:00"
   }
   ```

5. **Enviá la request:**
   - Click en "Send"
   - Si todo está bien, vas a recibir un 200 OK con los datos de la reserva creada

### Consultar disponibilidad en Postman:

1. **Método:** `GET`
2. **URL:** `http://localhost:4002/locations/1/availability`
3. **Params (pestaña Params):**
   - Key: `mealTime` | Value: `ALMUERZO`
   - Key: `date` | Value: `2025-11-02T00:00:00`
4. Click en "Send"

---

## 🔍 Ejemplos de casos de uso

### Caso 1: Flujo completo de reserva
```bash
# 1. Consultar ubicaciones disponibles
GET /locations

# 2. Verificar disponibilidad para ALMUERZO el 2025-11-02 en Norte
GET /locations/1/availability?mealTime=ALMUERZO&date=2025-11-02T00:00:00

# 3. Crear reserva si hay disponibilidad
POST /reservations
Body: {
  "userId": 1,
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationTimeSlot": "ALMUERZO_SLOT_1",
  "reservationDate": "2025-11-02T00:00:00"
}

# 4. Verificar que la reserva se creó
GET /reservations/mine?userId=1
```

### Caso 2: Reserva en otro horario
```json
{
  "userId": 2,
  "locationId": 2,
  "mealTime": "CENA",
  "reservationTimeSlot": "CENA_SLOT_2",
  "reservationDate": "2025-11-05T00:00:00"
}
```

### Caso 3: Reserva de DESAYUNO
```json
{
  "userId": 3,
  "locationId": 1,
  "mealTime": "DESAYUNO",
  "reservationTimeSlot": "DESAYUNO_SLOT_1",
  "reservationDate": "2025-11-03T00:00:00"
}
```

---

## ⚠️ Validaciones importantes

1. **Time slot debe corresponder al meal time:**
   - ❌ `mealTime: "ALMUERZO"` con `reservationTimeSlot: "CENA_SLOT_1"` → Error 400
   - ✅ `mealTime: "ALMUERZO"` con `reservationTimeSlot: "ALMUERZO_SLOT_1"` → OK

2. **Capacidad máxima:**
   - Cada location tiene capacidad de 10 asientos por hora
   - Si ya hay 10 reservas para ese slot/location/fecha → Error 409

3. **Estados de reserva:**
   - `ACTIVA`: Reserva creada y activa
   - `CONFIRMADA`: Reserva confirmada
   - `CANCELADA`: Reserva cancelada (no cuenta para capacidad)
   - `AUSENTE`: Usuario no se presentó

---

## 🎯 Tips para testing

1. **Para llenar un slot rápidamente:**
   - Creá 10 reservas con diferentes `userId` para el mismo slot/location/fecha
   - La 11va reserva debería fallar con 409

2. **Para probar diferentes horarios:**
   - Cambiá `reservationDate` para diferentes días
   - Probá todos los meal times y slots

3. **Para verificar disponibilidad después de crear reservas:**
   - Hacé GET a `/locations/{id}/availability` después de cada POST
   - Deberías ver que `availableSeats` disminuye

---

## 📊 Estado de las locations al iniciar

Cuando levantás la aplicación, se crean automáticamente 2 locations:
- **Location 1 - Norte**: Capacidad 10 asientos/hora
- **Location 2 - Sur**: Capacidad 10 asientos/hora

Ambas están disponibles para todos los meal times y slots.

---

## 📋 Changelog - Cambios Recientes

### 2025-11-06 - Fix endpoint `/menus/now` y zona horaria

#### Problema identificado
El endpoint `GET /menus/now` lanzaba un error 500 con la excepción:
```
org.springframework.dao.IncorrectResultSizeDataAccessException: Query did not return a unique result: 10 results were returned
```

Esto ocurría porque la consulta `findByDays_Day(MenuDay.DayOfWeek day)` en `MenuRepository` retornaba múltiples menús que contenían el día de la semana solicitado (por ejemplo, múltiples menús con día "LUNES"), pero el método esperaba un único resultado.

#### Cambios implementados

**1. Nuevo método en `MenuRepository`:**
```java
@EntityGraph(attributePaths = {"days", "days.meals", "days.meals.products"})
Optional<Menu> findTopByDays_DayOrderByLastModifiedDesc(MenuDay.DayOfWeek day);
```

Este método:
- Busca todos los menús que contengan el día especificado
- Los ordena por `lastModified` de forma descendente (el más reciente primero)
- Retorna solo el primero (el más reciente)
- Garantiza un resultado único o `Optional.empty()`

**2. Actualización de `MenuService`:**

Se reemplazaron todas las llamadas a `findByDays_Day(day)` por `findTopByDays_DayOrderByLastModifiedDesc(day)` en los siguientes métodos:
- `getMenuByDay(MenuDay.DayOfWeek day)`
- `getCurrentMenu()`
- `getMenuByDayAndMealTime(MenuDay.DayOfWeek day, MenuMeal.MealTime mealTime)`
- `updateDayMenu(MenuDay.DayOfWeek day, List<MenuMealCreateRequest> mealsRequest)`
- `updateMealMenu(MenuDay.DayOfWeek day, MenuMeal.MealTime mealTime, List<Long> productIds)`

**3. Zona horaria configurada:**

Ya estaba implementada en `ComedorApplication.java`:
```java
@PostConstruct
public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
}
```

Esto asegura que todas las operaciones con `LocalDateTime.now()` usen la hora de Argentina.

#### Cómo funciona el endpoint `/menus/now`

**Flujo completo:**

1. **Obtiene la fecha y hora actual** usando la zona horaria de Argentina
   ```java
   LocalDateTime now = LocalDateTime.now(); // Ej: 2025-11-06 13:15:00 (hora Argentina)
   ```

2. **Determina el día de la semana actual**
   ```java
   java.time.DayOfWeek today = now.getDayOfWeek(); // Ej: WEDNESDAY
   MenuDay.DayOfWeek menuDay = convertToMenuDay(today); // Convierte a: MIERCOLES
   ```

3. **Busca el menú más reciente para ese día**
   ```java
   Menu menu = menuRepository.findTopByDays_DayOrderByLastModifiedDesc(menuDay)
   ```
   - Si hay múltiples menús con día "MIERCOLES", toma el que tenga `lastModified` más reciente
   - Si no encuentra ninguno, lanza `404 - No hay menú disponible por el momento`

4. **Determina el turno de comida actual** basado en los horarios configurados
   ```java
   MenuMeal.MealTime currentMealTime = getCurrentMealTime(currentTime);
   ```
   - Consulta los horarios definidos en `MealTimeScheduleService`
   - Ejemplo: Si son las 13:15, determina que es `ALMUERZO`
   - Si está fuera de horario de comidas, lanza `404 - No hay menú disponible por el momento`

5. **Filtra el menú del día para devolver solo el turno actual**
   ```java
   MenuMeal currentMeal = currentMenuDay.getMeals().stream()
       .filter(m -> m.getMealTime().equals(currentMealTime))
       .findFirst()
       .orElseThrow(...)
   ```

6. **Devuelve el resultado** con el formato esperado:
   ```json
   {
       "mealTime": "ALMUERZO",
       "sections": {
           "platos": [...],
           "bebidas": [...],
           "postres": [...]
       }
   }
   ```

#### Respuestas del endpoint

**✅ Caso exitoso (200 OK):**
- Hay menú configurado para el día actual
- Estamos dentro del horario de un turno de comida (desayuno/almuerzo/merienda/cena)
- Retorna el menú con productos agrupados por tipo

**❌ Caso sin menú (404 NOT FOUND):**
```json
{
  "timestamp": "2025-11-06T16:13:13.941Z",
  "status": 404,
  "error": "Not Found",
  "message": "No hay menú disponible por el momento",
  "path": "/menus/now"
}
```

Esto ocurre cuando:
- No existe un menú para el día actual en la base de datos
- Es fin de semana (sábado/domingo) y no hay menús configurados
- Estamos fuera del horario de comidas (ej: 3 AM, 10 PM)
- Existe menú para el día pero no tiene configurado el turno actual

#### Ejemplo de uso en Postman

**Request:**
```http
GET http://localhost:8080/menus/now
```

**Response esperada (miércoles a las 13:00):**
```json
{
    "mealTime": "ALMUERZO",
    "sections": {
        "platos": [
            {
                "id": 3,
                "name": "Pizza",
                "description": "",
                "price": 10000.00,
                "productType": "PLATO",
                "imageUrl": ""
            }
        ],
        "bebidas": [
            {
                "id": 9,
                "name": "Coca Cola",
                "description": "500 ml Gaseosa",
                "price": 1500.00,
                "productType": "BEBIDA",
                "imageUrl": ""
            }
        ],
        "postres": [
            {
                "id": 2,
                "name": "Medialunas (3 unidades)",
                "description": "3 medialunas dulces",
                "price": 800.00,
                "productType": "POSTRE",
                "imageUrl": "https://example.com/medialunas.jpg"
            }
        ]
    }
}
```

#### Validaciones importantes

1. **Múltiples menús para el mismo día**: El sistema ahora selecciona automáticamente el más reciente
2. **Zona horaria**: Siempre usa hora de Argentina (UTC-3)
3. **Horarios de comida**: Configurados en `MealTimeScheduleService`:
   - DESAYUNO: 07:00 - 09:00
   - ALMUERZO: 12:00 - 14:00
   - MERIENDA: 16:00 - 18:00
   - CENA: 20:00 - 22:00

#### Archivos modificados

- `comedor/src/main/java/com/uade/comedor/repository/MenuRepository.java`
- `comedor/src/main/java/com/uade/comedor/service/MenuService.java`
- `comedor/src/main/java/com/uade/comedor/ComedorApplication.java` (ya existía la config de timezone)

---

### 2025-11-08 - Endpoint `/menus/demo/now` para desarrollo del frontend

#### Problema/Necesidad
Durante el desarrollo del frontend, el endpoint `/menus/now` solo funciona de lunes a viernes y dentro de los horarios de comida configurados. Esto dificulta el desarrollo en fines de semana o fuera de horario, ya que no se puede probar la funcionalidad sin datos reales.

#### Solución implementada
Se creó un nuevo endpoint **`POST /menus/demo/now`** que permite simular cualquier día de la semana y turno de comida sin validar la hora actual del sistema.

#### Características del nuevo endpoint

**Endpoint:** `POST /menus/demo/now`

**Request Body (JSON):**
```json
{
  "day": "LUNES",
  "mealTime": "ALMUERZO"
}
```

**Parámetros:**
- `day` (requerido): Día de la semana
  - Valores válidos: `LUNES`, `MARTES`, `MIERCOLES`, `JUEVES`, `VIERNES`
- `mealTime` (requerido): Turno de comida
  - Valores válidos: `DESAYUNO`, `ALMUERZO`, `MERIENDA`, `CENA`

**Response (200 OK):**
Mismo formato que `/menus/now`:
```json
{
    "mealTime": "ALMUERZO",
    "sections": {
        "platos": [
            {
                "id": 3,
                "name": "Pizza",
                "description": "",
                "price": 10000.00,
                "productType": "PLATO",
                "imageUrl": ""
            }
        ],
        "bebidas": [
            {
                "id": 9,
                "name": "Coca Cola",
                "description": "500 ml Gaseosa",
                "price": 1500.00,
                "productType": "BEBIDA",
                "imageUrl": ""
            }
        ],
        "postres": [
            {
                "id": 2,
                "name": "Medialunas (3 unidades)",
                "description": "3 medialunas dulces",
                "price": 800.00,
                "productType": "POSTRE",
                "imageUrl": "https://example.com/medialunas.jpg"
            }
        ]
    }
}
```

#### Ejemplos de uso en Postman

**Ejemplo 1: Menú del almuerzo del lunes**
```http
POST http://localhost:8080/menus/demo/now
Content-Type: application/json

{
  "day": "LUNES",
  "mealTime": "ALMUERZO"
}
```

**Ejemplo 2: Menú del desayuno del miércoles**
```http
POST http://localhost:8080/menus/demo/now
Content-Type: application/json

{
  "day": "MIERCOLES",
  "mealTime": "DESAYUNO"
}
```

**Ejemplo 3: Menú de la cena del viernes**
```http
POST http://localhost:8080/menus/demo/now
Content-Type: application/json

{
  "day": "VIERNES",
  "mealTime": "CENA"
}
```

#### Diferencias entre `/menus/now` y `/menus/demo/now`

| Característica | `/menus/now` (GET) | `/menus/demo/now` (POST) |
|----------------|-------------------|-------------------------|
| **Método HTTP** | GET | POST |
| **Parámetros** | Ninguno | `day` y `mealTime` en body |
| **Validación de hora** | ✅ Sí (solo horarios de comida) | ❌ No (siempre disponible) |
| **Validación de día** | ✅ Sí (solo lun-vie) | ❌ No (acepta cualquier día) |
| **Uso** | Producción | Desarrollo/Testing |
| **Output** | Menú actual según hora del sistema | Menú simulado según parámetros |

#### Casos de error (404 NOT FOUND)

```json
{
  "timestamp": "2025-11-08T13:11:56.941Z",
  "status": 404,
  "error": "Not Found",
  "message": "No hay menú disponible por el momento",
  "path": "/menus/demo/now"
}
```

Este error ocurre cuando:
- No existe un menú configurado para el día especificado
- El menú del día existe pero no tiene configurado el turno de comida especificado

#### Plan de migración para producción

Para la entrega final, el frontend debe:

1. **Durante desarrollo** (ahora):
   ```javascript
   // Usar endpoint demo con parámetros simulados
   const response = await fetch('/menus/demo/now', {
     method: 'POST',
     headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({ 
       day: 'LUNES', 
       mealTime: 'ALMUERZO' 
     })
   });
   ```

2. **Para producción** (entrega final):
   ```javascript
   // Cambiar a endpoint real sin parámetros
   const response = await fetch('/menus/now', {
     method: 'GET'
   });
   ```

El formato de respuesta es idéntico, por lo que no se requieren cambios en el código de procesamiento de datos.

#### Archivos creados/modificados

- **Nuevo:** `comedor/src/main/java/com/uade/comedor/dto/DemoMenuRequest.java`
- **Modificado:** `comedor/src/main/java/com/uade/comedor/controller/MenuController.java`
- **Modificado:** `comedor/src/main/java/com/uade/comedor/service/MenuService.java`

#### Notas importantes

⚠️ **Este endpoint es solo para desarrollo.** En producción, se recomienda:
- Usar `/menus/now` que valida día y horario real
- O bien deshabilitar `/menus/demo/now` en el ambiente de producción mediante configuración

✅ **El endpoint `/menus/now` original NO fue modificado** y mantiene su comportamiento correcto de validación de día y horario.

