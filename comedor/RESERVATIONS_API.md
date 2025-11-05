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
