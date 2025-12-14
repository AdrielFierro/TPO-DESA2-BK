# Cambios Realizados: userId de Long a String (UUID)

## Fecha: 9 de diciembre de 2025

## Resumen de Cambios

Se ha actualizado el sistema para soportar `userId` como **String (UUID)** en lugar de `Long`, preparando el sistema para la autenticación con JWT.

---

## 🔄 Entidades Modificadas

### 1. **Reservation.java**
- `userId`: `Long` → `String`

### 2. **Cart.java**
- `userId`: `Long` → `String`
- Valor por defecto temporal: `"00000000-0000-0000-0000-000000000000"`

### 3. **Bill.java**
- `userId`: `Long` → `String`

---

## 📋 DTOs Modificados

### 1. **CreateReservationRequest.java**
- `userId`: `Long` → `String`

### 2. **ReservationDateRangeRequest.java**
- `userId`: `Long` → `String`

### 3. **BillEventDTO.java**
- `userId`: `Long` → `String`

### 4. **ReservationEventDTO.java**
- `userId`: `Long` → `String`

---

## 🗄️ Repositorios Actualizados

### **ReservationRepository.java**
Todos los métodos que usaban `userId` ahora aceptan `String`:
- `findByUserId(String userId)`
- `findActiveAndRecentByUserId(String userId, LocalDateTime twoDaysAgo)`
- `findByUserIdAndDateBetween(String userId, LocalDateTime startDate, LocalDateTime endDate)`
- `countByUserIdAndMealTimeAndReservationDateBetween(String userId, ...)`

---

## 🔧 Servicios Actualizados

### **ReservationService.java**
- `getReservationsByUser(String userId)`
- `getActiveAndRecentReservationsByUser(String userId)`
- `getReservationsByUserAndDateRange(String userId, ...)`

### **CartService.java**
- Valor temporal de userId: `"00000000-0000-0000-0000-000000000000"`
- TODO: Obtener del JWT cuando esté implementado

---

## 🌐 Endpoints Modificados

### **ReservationController.java**

#### ✅ NUEVO: GET `/reservations/mine`
**Descripción:** Obtiene las reservas del usuario autenticado (extrae userId del JWT)

**Respuesta:**
```json
[
  {
    "reservationId": 1,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "locationId": 1,
    "mealTime": "ALMUERZO",
    ...
  }
]
```

**TODO:** Extraer userId del JWT en lugar del placeholder actual

---

#### ✅ NUEVO: GET `/reservations/userId/{userId}`
**Descripción:** Obtiene todas las reservas de un usuario específico por su UUID

**Ejemplo:**
```
GET http://127.0.0.1:4002/reservations/userId/550e8400-e29b-41d4-a716-446655440000
```

**Respuesta:**
```json
[
  {
    "reservationId": 1,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "locationId": 1,
    "mealTime": "ALMUERZO",
    "reservationDate": "2025-11-05T12:00:00",
    "status": "ACTIVA",
    "cost": 25.00,
    ...
  }
]
```

---

#### ❌ ELIMINADO: GET `/reservations/mine?userId=...`
Este endpoint fue reemplazado por los dos nuevos endpoints arriba

---

## 🔐 Integración con JWT (Pendiente)

### Pasos para completar la integración:

1. **Agregar dependencia de JWT** en `pom.xml`:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
```

2. **Crear JwtService** para extraer el userId del token

3. **Actualizar SecurityConfig** para validar JWT

4. **Modificar ReservationController.getMyReservations()**:
```java
@GetMapping("/mine")
public ResponseEntity<List<Reservation>> getMyReservations(
        @RequestHeader("Authorization") String authHeader) {
    String userId = jwtService.extractUserId(authHeader);
    List<Reservation> reservations = reservationService
        .getActiveAndRecentReservationsByUser(userId);
    return ResponseEntity.ok(reservations);
}
```

5. **Modificar CartService.createCart()**:
```java
// En lugar de:
cart.setUserId("00000000-0000-0000-0000-000000000000");

// Usar:
String userId = jwtService.extractUserIdFromContext();
cart.setUserId(userId);
```

---

## 🧪 Testing

### Ejemplos de prueba con UUIDs:

#### Crear una reserva:
```bash
POST http://127.0.0.1:4002/reservations
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationDate": "2025-12-10T12:00:00"
}
```

#### Obtener reservas por userId:
```bash
GET http://127.0.0.1:4002/reservations/userId/550e8400-e29b-41d4-a716-446655440000
```

#### Obtener mis reservas (con JWT):
```bash
GET http://127.0.0.1:4002/reservations/mine
Authorization: Bearer <JWT_TOKEN>
```

---

## 📊 Migración de Base de Datos

**IMPORTANTE:** Si ya tienes datos en la base de datos, necesitarás migrar los valores de `userId`:

```sql
-- Opción 1: Convertir IDs existentes a UUIDs
UPDATE reservations SET userId = CONCAT(
    LPAD(HEX(userId), 8, '0'), '-',
    '0000-0000-0000-000000000000'
) WHERE userId IS NOT NULL;

UPDATE carts SET userId = CONCAT(
    LPAD(HEX(userId), 8, '0'), '-',
    '0000-0000-0000-000000000000'
) WHERE userId IS NOT NULL;

UPDATE bills SET userId = CONCAT(
    LPAD(HEX(userId), 8, '0'), '-',
    '0000-0000-0000-000000000000'
) WHERE userId IS NOT NULL;

-- Opción 2: Limpiar datos de prueba
TRUNCATE TABLE reservations;
TRUNCATE TABLE carts;
TRUNCATE TABLE bills;
```

---

## ✅ Estado Actual

- ✅ Todas las entidades actualizadas
- ✅ Todos los DTOs actualizados
- ✅ Todos los repositorios actualizados
- ✅ Todos los servicios actualizados
- ✅ Endpoints separados correctamente
- ⏳ Pendiente: Integración con JWT
- ⏳ Pendiente: Migración de datos existentes

---

## 🚀 Próximos Pasos

1. Implementar servicio de JWT
2. Actualizar SecurityConfig para validar tokens
3. Modificar endpoints para extraer userId del JWT
4. Probar flujo completo con autenticación
5. Migrar datos existentes si es necesario
