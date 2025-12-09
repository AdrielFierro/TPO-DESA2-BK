# 🔐 Autenticación JWT en Reservas

## 📋 Descripción

El endpoint `/reservations/mine` ahora requiere autenticación JWT obligatoriamente. El `userId` se extrae automáticamente del token JWT (campo `sub`).

---

## 🔑 Configuración del JWT Secret

El secret para validar el JWT se configura en:

```properties
# application.properties o application-local.properties
jwt.secret=a-string-secret-at-least-256-bits-long
```

⚠️ **Importante:** El secret debe tener al menos 256 bits (32 caracteres) para HS256.

---

## 🧪 Ejemplo de JWT

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwMGRlYmUzMi1hYmQyLTQ1YTgtYmVjZS0zZDNiNzUyZmExNDAiLCJlbWFpbCI6ImpwZXBlQGNhbXB1c2Nvbm5lY3QuZWR1LmFyIiwibmFtZSI6Ikp1YW4gUGVwZSIsInJvbGUiOiJBTFVNTk8iLCJjYXJlZXIiOnsidXVpZCI6IlBsYWNlaG9sZGVyIiwibmFtZSI6IlBsYWNlaG9sZGVyIn0sIndhbGxldCI6WyI4ZDk4MWFhMy1iM2ExLTQ2YWMtYmM5NC00ZTk1MjA5MDAyY2QiXSwiaWF0IjoxNzY1MzIxNzI5LCJleHAiOjE3NjUzMzA3Mjl9.PdakbaQ6tLC8x-pt2JGmHz9Ni__vmWRYuTDgi_RJ_4k
```

### Payload decodificado:
```json
{
  "sub": "00debe32-abd2-45a8-bece-3d3b752fa140",
  "email": "jpepe@campusconnect.edu.ar",
  "name": "Juan Pepe",
  "role": "ALUMNO",
  "career": {
    "uuid": "Placeholder",
    "name": "Placeholder"
  },
  "wallet": [
    "8d981aa3-b3a1-46ac-bc94-4e95209002cd"
  ],
  "iat": 1765321729,
  "exp": 1765330729
}
```

---

## 🚀 Endpoints

### ✅ GET `/reservations/mine` (Requiere JWT)

Obtiene las reservas del usuario autenticado. El `userId` se extrae automáticamente del JWT.

**Headers requeridos:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Ejemplo con curl:**
```bash
curl -X GET http://127.0.0.1:4002/reservations/mine \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwMGRlYmUzMi1hYmQyLTQ1YTgtYmVjZS0zZDNiNzUyZmExNDAiLCJlbWFpbCI6ImpwZXBlQGNhbXB1c2Nvbm5lY3QuZWR1LmFyIiwibmFtZSI6Ikp1YW4gUGVwZSIsInJvbGUiOiJBTFVNTk8iLCJjYXJlZXIiOnsidXVpZCI6IlBsYWNlaG9sZGVyIiwibmFtZSI6IlBsYWNlaG9sZGVyIn0sIndhbGxldCI6WyI4ZDk4MWFhMy1iM2ExLTQ2YWMtYmM5NC00ZTk1MjA5MDAyY2QiXSwiaWF0IjoxNzY1MzIxNzI5LCJleHAiOjE3NjUzMzA3Mjl9.PdakbaQ6tLC8x-pt2JGmHz9Ni__vmWRYuTDgi_RJ_4k"
```

**Respuesta exitosa (200 OK):**
```json
[
  {
    "id": 1,
    "userId": "00debe32-abd2-45a8-bece-3d3b752fa140",
    "locationId": 1,
    "mealTime": "ALMUERZO",
    "reservationTimeSlot": "ALMUERZO_SLOT_1",
    "reservationDate": "2025-12-10T12:00:00",
    "status": "ACTIVA",
    "cost": 25.00,
    "createdAt": "2025-12-09T20:00:00",
    "slotStartTime": "12:00:00",
    "slotEndTime": "13:00:00"
  }
]
```

**Respuesta sin JWT (401 Unauthorized):**
```json
{
  "timestamp": "2025-12-09T20:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/reservations/mine"
}
```

**Respuesta con JWT inválido (403 Forbidden):**
```json
{
  "timestamp": "2025-12-09T20:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/reservations/mine"
}
```

---

### 🔓 GET `/reservations/userId/{userId}` (Sin autenticación)

Obtiene todas las reservas de un usuario específico por su UUID. No requiere autenticación.

**Ejemplo con curl:**
```bash
curl -X GET http://127.0.0.1:4002/reservations/userId/00debe32-abd2-45a8-bece-3d3b752fa140
```

---

### 🔓 POST `/reservations` (Sin autenticación)

Crea una nueva reserva. No requiere autenticación (por ahora).

**Ejemplo con curl:**
```bash
curl -X POST http://127.0.0.1:4002/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "00debe32-abd2-45a8-bece-3d3b752fa140",
    "locationId": 1,
    "mealTime": "ALMUERZO",
    "reservationDate": "2025-12-10T12:00:00"
  }'
```

---

## 🔧 Componentes Implementados

### 1. **JwtService** 
Servicio para validar y extraer información del JWT.

**Métodos:**
- `extractUserId(token)` - Extrae el userId (sub)
- `extractEmail(token)` - Extrae el email
- `extractName(token)` - Extrae el nombre
- `extractRole(token)` - Extrae el rol
- `validateToken(token)` - Valida si el token es válido

### 2. **JwtAuthenticationFilter**
Filtro que intercepta las peticiones y valida el JWT en el header `Authorization`.

**Funcionamiento:**
1. Extrae el token del header `Authorization: Bearer <token>`
2. Valida el token usando `JwtService`
3. Si es válido, establece la autenticación en el `SecurityContext`
4. Si no es válido o no existe, continúa sin autenticación

### 3. **SecurityConfig**
Configuración de seguridad que:
- Requiere JWT para `/reservations/mine`
- Permite acceso sin autenticación al resto de endpoints
- Agrega el filtro JWT antes del filtro de autenticación de Spring

---

## 🧪 Testing con Postman

### 1. Crear una variable con el JWT

En Postman, crea una variable de entorno:
```
jwt_token = eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2. Configurar el Header

En la pestaña **Headers**:
```
Authorization: Bearer {{jwt_token}}
```

### 3. Hacer la petición

```
GET http://127.0.0.1:4002/reservations/mine
```

---

## ⚠️ Notas Importantes

1. **Secret Key:** El secret debe ser el mismo que se usa para firmar el JWT en el módulo de autenticación.

2. **Expiración:** El JWT tiene un tiempo de expiración (`exp`). Si el token está expirado, la validación fallará.

3. **Claims requeridos:** El JWT debe contener al menos el claim `sub` con el userId (UUID).

4. **Formato del userId:** El userId debe ser un UUID válido (formato: `00debe32-abd2-45a8-bece-3d3b752fa140`).

---

## 🔄 Próximos pasos (TODO)

- [ ] Agregar autenticación JWT también a `POST /reservations`
- [ ] Agregar autenticación JWT a `POST /carts`
- [ ] Validar que el userId del JWT coincida con el userId de la reserva al cancelar
- [ ] Agregar roles y permisos (ALUMNO, ADMIN, etc.)
- [ ] Implementar refresh tokens
