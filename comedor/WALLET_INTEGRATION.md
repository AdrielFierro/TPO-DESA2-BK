# Integración de Wallet - Documentación

## Resumen

Se ha integrado el sistema de pagos mediante Wallet en los endpoints de reservas y pedidos de comida.

## API de Wallet

**Base URL:** `https://jtseq9puk0.execute-api.us-east-1.amazonaws.com`

**Endpoint de Transferencias:** `POST /api/transfers`

**Request Body:**
```json
{
  "from": "893cb495-9a0f-4fe3-952f-349b75301194",
  "to": "SYSTEM",
  "currency": "ARG",
  "amount": 1000,
  "type": "credit",
  "description": "Transferencia recibida"
}
```

## Extracción del Wallet ID

El `walletId` se extrae del token JWT del usuario. El token contiene un campo `wallet` que es un array:

```json
{
  "sub": "fcf52897-5b80-4e8b-8680-43d8ba0a129a",
  "email": "jchef@campusconnect.edu.ar",
  "name": "Juan Chef",
  "role": "ADMINISTRADOR",
  "subrol": "CHEF",
  "career": null,
  "wallet": [
    "893cb495-9a0f-4fe3-952f-349b75301194"
  ],
  "iat": 1765674617,
  "exp": 1765683617
}
```

## Cambios Implementados

### 1. Nuevos DTOs

#### `WalletTransferRequest.java`
- DTO para enviar solicitudes de transferencia a la API de Wallet

#### `WalletTransferResponse.java`
- DTO para recibir respuestas de la API de Wallet

### 2. Nuevo Servicio: `WalletService.java`

Métodos principales:
- `chargeUser(String userWalletId, BigDecimal amount, String description)`: Cobra al usuario
- `chargeReservation(String userWalletId, BigDecimal amount, Long reservationId)`: Cobra una reserva
- `chargeOrder(String userWalletId, BigDecimal amount, Long billId)`: Cobra un pedido

### 3. Actualización de `JwtService.java`

- Nuevo método: `extractWalletId(String token)` - Extrae el wallet UUID del JWT

### 4. Nuevo Token de Autenticación: `UserAuthenticationToken.java`

- Extiende `UsernamePasswordAuthenticationToken`
- Incluye el campo `walletId` para almacenar el UUID de la wallet del usuario

### 5. Actualización de `JwtAuthenticationFilter.java`

- Ahora usa `UserAuthenticationToken` en lugar de `UsernamePasswordAuthenticationToken`
- Extrae y almacena el `walletId` en el contexto de autenticación

### 6. Actualización de `ReservationController.java`

- El endpoint `POST /reservations` ahora:
  1. Extrae el `walletId` del contexto de autenticación
  2. Lo pasa al servicio para realizar el cobro

### 7. Actualización de `ReservationService.java`

- El método `createReservation` ahora:
  1. Recibe el `walletId` como parámetro
  2. Realiza el cobro ANTES de crear la reserva
  3. Si el cobro falla, lanza una excepción y no crea la reserva

### 8. Actualización de `CartController.java`

- El endpoint `POST /carts/confirmation/{id}` ahora:
  1. Extrae el `walletId` del contexto de autenticación
  2. Lo pasa al servicio para realizar el cobro

### 9. Actualización de `CartService.java`

- El método `confirmCart` ahora:
  1. Recibe el `walletId` como parámetro
  2. Realiza el cobro ANTES de confirmar el carrito
  3. Si el cobro falla, lanza una excepción y no confirma el carrito

### 10. Configuración

**`application.properties`:**
```properties
# Wallet API Configuration
wallet.api.url=https://jtseq9puk0.execute-api.us-east-1.amazonaws.com
wallet.system.account=SYSTEM
```

**`application.properties` (test):**
```properties
# Wallet API Configuration - dummy para tests
wallet.api.url=http://localhost:8080
wallet.system.account=SYSTEM
```

## Flujo de Pago

### Para Reservas:

1. Usuario hace `POST /reservations` con el token JWT en el header
2. El filtro JWT extrae el `walletId` del token y lo almacena en el contexto
3. El controller obtiene el `walletId` del contexto
4. El service realiza el cobro mediante `WalletService.chargeReservation()`
5. Si el cobro es exitoso, se crea la reserva
6. Si el cobro falla, se lanza una excepción `PAYMENT_REQUIRED` (402)

### Para Pedidos:

1. Usuario crea un carrito con `POST /carts`
2. Usuario confirma el carrito con `POST /carts/confirmation/{id}` con token JWT
3. El filtro JWT extrae el `walletId` del token
4. El controller obtiene el `walletId` del contexto
5. El service realiza el cobro mediante `WalletService.chargeOrder()`
6. Si el cobro es exitoso, se confirma el carrito y se crea la factura
7. Si el cobro falla, se lanza una excepción `PAYMENT_REQUIRED` (402)

## Manejo de Errores

- **Sin wallet en el token:** `RuntimeException: "El usuario no tiene una wallet asociada"`
- **Fallo en el cobro:** `ResponseStatusException: PAYMENT_REQUIRED (402)` con el mensaje de error de la API de Wallet
- **Error de conexión:** `RuntimeException: "Error al conectar con la API de Wallet"`

## Testing

Para testing, las propiedades apuntan a un URL dummy (`http://localhost:8080`). En tests deberás mockear el `WalletService` o proporcionar un servidor de wallet de prueba.

## Seguridad

⚠️ **IMPORTANTE:** El sistema actualmente **NO valida la firma del JWT**. Esto debe corregirse en producción implementando la validación con la clave pública del servicio de autenticación.

## Próximos Pasos

1. Implementar validación de firma JWT en producción
2. Agregar logs de auditoría para las transacciones
3. Implementar retry logic para fallos transitorios de red
4. Agregar tests unitarios y de integración para los flujos de pago
