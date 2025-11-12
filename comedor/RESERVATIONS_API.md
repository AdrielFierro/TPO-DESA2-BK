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
  - **DESAYUNO**: `DESAYUNO_SLOT_1` (7-8h), `DESAYUNO_SLOT_2` (8-9h), `DESAYUNO_SLOT_3` (9-10h), `DESAYUNO_SLOT_4` (10-11h), `DESAYUNO_SLOT_5` (11-12h)
  - **ALMUERZO**: `ALMUERZO_SLOT_1` (12-13h), `ALMUERZO_SLOT_2` (13-14h), `ALMUERZO_SLOT_3` (14-15h), `ALMUERZO_SLOT_4` (15-16h)
  - **MERIENDA**: `MERIENDA_SLOT_1` (16-17h), `MERIENDA_SLOT_2` (17-18h), `MERIENDA_SLOT_3` (18-19h), `MERIENDA_SLOT_4` (19-20h)
  - **CENA**: `CENA_SLOT_1` (20-21h), `CENA_SLOT_2` (21-22h), `CENA_SLOT_3` (22-23h)
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

**Respuesta de error - Fecha no coincide (400 BAD REQUEST):**
```json
{
  "timestamp": "2025-11-12T14:50:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La reserva no coincide con el día de la fecha, no puede avanzar. La fecha de la reserva es: 2025-11-15",
  "path": "/reservations/byreservationId/1"
}
```

**Respuesta de error - Reserva vencida (400 BAD REQUEST):**
```json
{
  "timestamp": "2025-11-12T14:50:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La reserva está vencida. La fecha y hora de la reserva era: 2025-11-12T10:00:00",
  "path": "/reservations/byreservationId/1"
}
```

**Nota importante:** Este endpoint valida:
1. **Reserva vencida**: Si la fecha/hora de la reserva ya pasó, retorna error indicando que está vencida
2. **Día diferente**: Si la reserva es para otro día (futuro), retorna error indicando que no coincide con el día actual

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

---

### 2025-11-12 - Validación de fecha en consulta de reserva por ID

#### Problema/Necesidad
Al buscar una reserva por ID mediante el endpoint `GET /reservations/byreservationId/{reservationId}`, se necesitaba validar que la reserva corresponda al día actual. Esto previene que usuarios intenten avanzar con reservas que no son del día de hoy.

#### Solución implementada
Se agregó validación en el método `getReservationById()` del servicio `ReservationService` que:
1. **Primero** verifica si la reserva está vencida (fecha/hora anterior al momento actual)
2. **Luego** verifica si la fecha es de otro día (futuro)
3. Si alguna validación falla, lanza un error 400 BAD REQUEST con mensaje específico
4. El mensaje de error incluye la fecha/hora real de la reserva

#### Comportamiento del endpoint

**Endpoint afectado:** `GET /reservations/byreservationId/{reservationId}`

**Caso exitoso (200 OK):**
- La reserva existe
- La fecha de la reserva coincide con el día actual
- La hora de la reserva NO ha pasado aún
- Retorna los datos completos de la reserva

**Caso de error - Reserva vencida (400 BAD REQUEST):**
```json
{
  "timestamp": "2025-11-12T15:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La reserva está vencida. La fecha y hora de la reserva era: 2025-11-12T12:00:00",
  "path": "/reservations/byreservationId/1"
}
```
Este error ocurre cuando la fecha/hora de la reserva ya pasó (ej: son las 15:30 y la reserva era a las 12:00).

**Caso de error - Fecha no coincide (400 BAD REQUEST):**
```json
{
  "timestamp": "2025-11-12T14:50:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La reserva no coincide con el día de la fecha, no puede avanzar. La fecha de la reserva es: 2025-11-15",
  "path": "/reservations/byreservationId/1"
}
```
Este error ocurre cuando la reserva es para un día futuro.

**Caso de error - Reserva no encontrada (404 NOT FOUND):**
```json
{
  "timestamp": "2025-11-12T14:50:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Reservation not found",
  "path": "/reservations/byreservationId/999"
}
```

#### Ejemplos de uso

**Ejemplo 1: Consultar reserva del día actual y hora futura (éxito)**
```http
GET /reservations/byreservationId/1
```
Suponiendo que ahora son las 11:00 del 2025-11-12 y la reserva 1 es para las 12:00 del mismo día:
```json
{
  "id": 1,
  "userId": 5,
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationTimeSlot": "ALMUERZO_SLOT_1",
  "reservationDate": "2025-11-12T12:00:00",
  "status": "ACTIVA",
  "cost": 25.0,
  "createdAt": "2025-11-11T15:30:00",
  "slotStartTime": "12:00:00",
  "slotEndTime": "13:00:00"
}
```

**Ejemplo 2: Consultar reserva vencida del mismo día (error)**
```http
GET /reservations/byreservationId/2
```
Suponiendo que ahora son las 15:00 del 2025-11-12 y la reserva 2 era para las 12:00 del mismo día:
```json
{
  "timestamp": "2025-11-12T18:00:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La reserva está vencida. La fecha y hora de la reserva era: 2025-11-12T12:00:00",
  "path": "/reservations/byreservationId/2"
}
```

**Ejemplo 3: Consultar reserva de otro día futuro (error)**
```http
GET /reservations/byreservationId/3
```
Suponiendo que hoy es 2025-11-12 pero la reserva 3 es para el 2025-11-15:
```json
{
  "timestamp": "2025-11-12T17:50:23.123Z",
  "status": 400,
  "error": "Bad Request",
  "message": "La reserva no coincide con el día de la fecha, no puede avanzar. La fecha de la reserva es: 2025-11-15",
  "path": "/reservations/byreservationId/3"
}
```

#### Casos de uso típicos

1. **Escaneo de QR en el comedor:**
   - Usuario escanea QR con su reserva del día
   - Sistema consulta `/reservations/byreservationId/{id}`
   - Si es del día actual → permite avanzar con el proceso
   - Si es de otro día → muestra mensaje de error con la fecha correcta

2. **Verificación de asistencia:**
   - Personal del comedor consulta reserva
   - Solo permite confirmar asistencia si es del día actual
   - Previene errores de confirmación anticipada o tardía

3. **Control de acceso:**
   - Usuario intenta acceder con reserva antigua o futura
   - Sistema rechaza y muestra cuándo es su reserva real

#### Lógica de validación

```java
LocalDateTime now = LocalDateTime.now(); // Usa timezone de Argentina configurado

// 1. Primero verifica si la reserva está vencida (fecha/hora pasada)
if (reservation.getReservationDate().isBefore(now)) {
    // Error: reserva vencida
    throw new ResponseStatusException(400, 
        "La reserva está vencida. La fecha y hora de la reserva era: " + reservationDate);
}

// 2. Luego verifica si es de otro día (futuro)
LocalDate today = now.toLocalDate();
LocalDate reservationDay = reservation.getReservationDate().toLocalDate();

if (!reservationDay.equals(today)) {
    // Error: fecha no coincide (día futuro)
    throw new ResponseStatusException(400,
        "La reserva no coincide con el día de la fecha, no puede avanzar. " +
        "La fecha de la reserva es: " + reservationDay);
}
```

**Orden de validaciones:**
1. **Primero**: Reserva vencida (fecha/hora anterior a ahora)
2. **Segundo**: Día diferente (fecha futura)

Esto garantiza que:
- Una reserva de las 10:00 de hoy consultada a las 15:00 → "vencida"
- Una reserva de mañana consultada hoy → "no coincide con el día"
- Una reserva de ayer → "vencida" (porque su fecha/hora ya pasó)

#### Impacto en otros endpoints

Este cambio **SOLO** afecta a:
- ✅ `GET /reservations/byreservationId/{reservationId}`

**NO** afecta a:
- ❌ `GET /reservations` (listar todas)
- ❌ `GET /reservations/mine?userId={id}` (listar del usuario)
- ❌ `POST /reservations/mine` (buscar por rango de fechas)
- ❌ `POST /reservations` (crear nueva reserva)
- ❌ `DELETE /reservations/{id}` (cancelar)

Estos otros endpoints pueden mostrar reservas de cualquier fecha sin restricción.

#### Archivos modificados

- **Modificado:** `comedor/src/main/java/com/uade/comedor/service/ReservationService.java`
- **Actualizado:** `comedor/RESERVATIONS_API.md` (esta documentación)

#### Consideraciones de zona horaria

La validación usa `LocalDateTime.now()` que respeta la configuración de zona horaria de Argentina establecida en:
```java
// ComedorApplication.java
TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
```

Esto garantiza que "hoy" se refiera al día actual en Argentina, no UTC.

---

### 2025-11-12 - Endpoint para listar todos los carritos

#### Problema/Necesidad
El frontend necesita poder listar todos los carritos existentes en el sistema para:
- Mostrar carritos activos del usuario
- Identificar si existe un carrito abierto (OPEN) que pueda editarse
- Permitir al usuario ver su historial de carritos
- Facilitar la decisión entre crear un nuevo carrito o editar uno existente

#### Solución implementada
Se agregó el endpoint `GET /carts` que retorna todos los carritos existentes en el sistema.

**Endpoint:** `GET /carts`

**Método HTTP:** GET

**Parámetros:** Ninguno

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "billId": 5,
    "reservationId": 3,
    "reservationDiscount": 25.00,
    "paymentMethod": "SALDOCUENTA",
    "status": "CONFIRMED",
    "total": 2475.00,
    "createdAt": "2025-11-12T12:00:00",
    "products": [
      {
        "id": 1,
        "name": "Pizza",
        "price": 1000.00,
        "productType": "PLATO"
      }
    ]
  },
  {
    "id": 2,
    "userId": 1,
    "billId": null,
    "reservationId": null,
    "reservationDiscount": 0.00,
    "paymentMethod": "EFECTIVO",
    "status": "OPEN",
    "total": 1500.00,
    "createdAt": "2025-11-12T14:30:00",
    "products": [...]
  }
]
```

#### Estados de carritos

Los carritos pueden tener los siguientes estados:
- **OPEN**: Carrito activo, puede editarse
- **CONFIRMED**: Carrito confirmado, factura generada
- **CANCELLED**: Carrito cancelado por el usuario

#### Ejemplos de uso en Postman

**Request:**
```http
GET http://localhost:8080/carts
```

**Response con carritos existentes:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "status": "CONFIRMED",
    "total": 2500.00,
    "paymentMethod": "EFECTIVO"
  },
  {
    "id": 2,
    "userId": 1,
    "status": "OPEN",
    "total": 1500.00,
    "paymentMethod": "SALDOCUENTA"
  }
]
```

**Response con base de datos vacía:**
```json
[]
```

#### Uso en el frontend

**Flujo recomendado para decidir entre crear o editar:**

```javascript
// 1. Obtener todos los carritos
const response = await fetch('/carts');
const carts = await response.json();

// 2. Buscar si existe un carrito OPEN del usuario actual
const openCart = carts.find(cart => 
  cart.userId === currentUserId && 
  cart.status === 'OPEN'
);

if (openCart) {
  // Existe un carrito abierto → ACTUALIZAR
  await fetch(`/carts/${openCart.id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      cart: [1, 2, 3],
      paymentMethod: 'EFECTIVO'
    })
  });
} else {
  // No existe carrito abierto → CREAR NUEVO
  await fetch('/carts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      cart: [1, 2, 3],
      paymentMethod: 'EFECTIVO'
    })
  });
}
```

**Mostrar historial de carritos del usuario:**

```javascript
const carts = await (await fetch('/carts')).json();

// Filtrar carritos del usuario actual
const myCarts = carts.filter(cart => cart.userId === currentUserId);

// Separar por estado
const openCarts = myCarts.filter(c => c.status === 'OPEN');
const confirmedCarts = myCarts.filter(c => c.status === 'CONFIRMED');
const cancelledCarts = myCarts.filter(c => c.status === 'CANCELLED');

console.log(`Carritos abiertos: ${openCarts.length}`);
console.log(`Carritos confirmados: ${confirmedCarts.length}`);
console.log(`Carritos cancelados: ${cancelledCarts.length}`);
```

**Verificar si hay carrito activo:**

```javascript
const hasOpenCart = async (userId) => {
  const carts = await (await fetch('/carts')).json();
  return carts.some(cart => 
    cart.userId === userId && 
    cart.status === 'OPEN'
  );
};

if (await hasOpenCart(currentUserId)) {
  console.log('Ya tienes un carrito activo');
} else {
  console.log('Puedes crear un nuevo carrito');
}
```

#### Casos de uso

**1. Listar carritos del usuario:**
```javascript
const myCarts = carts.filter(cart => cart.userId === currentUserId);
// Mostrar en la UI
```

**2. Encontrar carrito abierto:**
```javascript
const openCart = carts.find(c => c.status === 'OPEN' && c.userId === userId);
if (openCart) {
  // Continuar editando este carrito
}
```

**3. Mostrar total de carritos confirmados:**
```javascript
const confirmedTotal = carts
  .filter(c => c.status === 'CONFIRMED')
  .reduce((sum, cart) => sum + cart.total, 0);
console.log(`Total gastado: $${confirmedTotal}`);
```

**4. Verificar si hay carritos con descuento activo:**
```javascript
const cartsWithDiscount = carts.filter(c => 
  c.reservationDiscount > 0 && 
  c.status === 'OPEN'
);
```

#### Información retornada en cada carrito

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long | ID único del carrito |
| `userId` | Long | ID del usuario propietario |
| `billId` | Long | ID de la factura asociada (null si no está confirmado) |
| `reservationId` | Long | ID de la reserva que da descuento (null si no aplica) |
| `reservationDiscount` | BigDecimal | Monto de descuento aplicado por reserva |
| `paymentMethod` | String | Método de pago: SALDOCUENTA, EFECTIVO, TRANSFERENCIA |
| `status` | String | Estado: OPEN, CONFIRMED, CANCELLED |
| `total` | BigDecimal | Total del carrito (subtotal - descuento) |
| `createdAt` | LocalDateTime | Fecha y hora de creación |
| `products` | List | Lista de productos en el carrito |

#### Consideraciones

✅ **Ventajas:**
- Permite al frontend tener visibilidad completa de todos los carritos
- Facilita la lógica de decisión entre crear/editar
- Útil para mostrar historial al usuario
- Simple de implementar y usar

⚠️ **Limitaciones:**
- Retorna TODOS los carritos del sistema (de todos los usuarios)
- En producción con muchos usuarios, podría ser pesado
- No tiene paginación ni filtros

💡 **Recomendaciones para producción:**
- Agregar filtro por userId: `GET /carts?userId={id}`
- Implementar paginación: `GET /carts?page=1&size=10`
- Filtrar por estado: `GET /carts?status=OPEN`
- Limitar resultados a los últimos N días

#### Mejoras futuras sugeridas

**Endpoint con filtros (ejemplo):**
```java
@GetMapping
public ResponseEntity<List<Cart>> getAllCarts(
    @RequestParam(required = false) Long userId,
    @RequestParam(required = false) Cart.CartStatus status
) {
    if (userId != null && status != null) {
        return ResponseEntity.ok(cartService.getCartsByUserIdAndStatus(userId, status));
    } else if (userId != null) {
        return ResponseEntity.ok(cartService.getCartsByUserId(userId));
    } else if (status != null) {
        return ResponseEntity.ok(cartService.getCartsByStatus(status));
    }
    return ResponseEntity.ok(cartService.getAllCarts());
}
```

#### Archivos modificados

- **Modificado:** `comedor/src/main/java/com/uade/comedor/service/CartService.java`
- **Modificado:** `comedor/src/main/java/com/uade/comedor/controller/CartController.java`
- **Actualizado:** `comedor/RESERVATIONS_API.md` (esta documentación)

---

### 2025-11-12 - Sincronización de horarios de comidas con Frontend

#### Problema identificado
Los horarios configurados en el backend no coincidían con los horarios mostrados en el frontend. Por ejemplo, el backend permitía desayuno solo hasta las 09:00, pero el frontend ofrecía slots hasta las 12:00. Esto causaba errores de validación cuando los usuarios intentaban hacer reservas en horarios válidos según el frontend.

#### Cambios implementados

**Actualización de `MealTimeScheduleService.java`:**

Los horarios se actualizaron para coincidir exactamente con el frontend:

| Comida | Horario Anterior | Horario Nuevo | Slots Disponibles |
|--------|------------------|---------------|-------------------|
| **DESAYUNO** | 07:00 - 09:00 | 07:00 - 12:00 | 5 slots de 1 hora |
| **ALMUERZO** | 12:00 - 14:00 | 12:00 - 16:00 | 4 slots de 1 hora |
| **MERIENDA** | 16:00 - 18:00 | 16:00 - 20:00 | 4 slots de 1 hora |
| **CENA** | 20:00 - 22:00 | 20:00 - 23:00 | 3 slots de 1 hora |

**Detalle de slots por comida:**

```java
// DESAYUNO: 07:00 - 12:00
DESAYUNO_SLOT_1: 07:00 - 08:00
DESAYUNO_SLOT_2: 08:00 - 09:00
DESAYUNO_SLOT_3: 09:00 - 10:00
DESAYUNO_SLOT_4: 10:00 - 11:00
DESAYUNO_SLOT_5: 11:00 - 12:00

// ALMUERZO: 12:00 - 16:00
ALMUERZO_SLOT_1: 12:00 - 13:00
ALMUERZO_SLOT_2: 13:00 - 14:00
ALMUERZO_SLOT_3: 14:00 - 15:00
ALMUERZO_SLOT_4: 15:00 - 16:00

// MERIENDA: 16:00 - 20:00
MERIENDA_SLOT_1: 16:00 - 17:00
MERIENDA_SLOT_2: 17:00 - 18:00
MERIENDA_SLOT_3: 18:00 - 19:00
MERIENDA_SLOT_4: 19:00 - 20:00

// CENA: 20:00 - 23:00
CENA_SLOT_1: 20:00 - 21:00
CENA_SLOT_2: 21:00 - 22:00
CENA_SLOT_3: 22:00 - 23:00
```

#### Impacto

✅ **Resuelto:** Ahora los usuarios pueden hacer reservas en todos los horarios mostrados en el frontend
✅ **Validación correcta:** El backend acepta reservas como "DESAYUNO_SLOT_5" (11:00-12:00) que antes eran rechazadas
✅ **Sincronización:** Frontend y backend están completamente alineados

⚠️ **Nota importante:** Si ya tenés datos en la base de datos con los horarios antiguos, vas a necesitar:
1. Eliminar la tabla `meal_time_schedules` o limpiar su contenido
2. Reiniciar la aplicación para que se inicialicen los nuevos horarios
3. O actualizar manualmente los registros existentes usando el endpoint PUT de MealTimeSchedule

#### Archivos modificados

- **Modificado:** `comedor/src/main/java/com/uade/comedor/service/MealTimeScheduleService.java`
- **Actualizado:** `comedor/RESERVATIONS_API.md` (esta documentación)


