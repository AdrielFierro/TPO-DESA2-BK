# 📚 API Documentation - Comedor UADE

Documentación completa de todos los endpoints del sistema de comedor universitario.

**Base URL**: `http://localhost:4002`

---

## 📋 Tabla de Contenidos

1. [Reservations (Reservas)](#reservations)
2. [Products (Productos)](#products)
3. [Menus (Menús)](#menus)
4. [Locations (Ubicaciones)](#locations)
5. [Carts (Carritos)](#carts)
6. [Bills (Facturas)](#bills)
7. [Meal Schedules (Horarios de Comida)](#meal-schedules)
8. [Sessions (Sesiones)](#sessions)

---

## 🍽️ Reservations

### 1. Crear Reserva

**Descripción**: Crea una nueva reserva para un usuario en una ubicación específica.

**Endpoint**: `POST /reservations`

**Body**:
```json
{
  "userId": 10,
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationTimeSlot": "ALMUERZO_SLOT_1",
  "reservationDate": "2025-11-06T00:00:00"
}
```

**Campos**:
- `userId` (Long): ID del usuario que hace la reserva
- `locationId` (Long): ID de la ubicación (1=Norte, 2=Centro, 3=Sur)
- `mealTime` (String): Tipo de comida - `"DESAYUNO"`, `"ALMUERZO"`, `"MERIENDA"`, `"CENA"`
- `reservationTimeSlot` (String): Slot de tiempo específico (ej: `"ALMUERZO_SLOT_1"`, `"ALMUERZO_SLOT_2"`)
- `reservationDate` (DateTime): Fecha y hora de la reserva

**Respuesta exitosa** (200 OK):
```json
{
  "id": 123,
  "userId": 10,
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationTimeSlot": "ALMUERZO_SLOT_1",
  "reservationDate": "2025-11-06T00:00:00",
  "status": "ACTIVA",
  "createdAt": "2025-11-01T21:00:00"
}
```

**Postman**:
- Method: `POST`
- URL: `http://localhost:4002/reservations`
- Headers: `Content-Type: application/json`
- Body → raw → JSON

**Evento RabbitMQ**: Publica evento `reservation.updated` con action `created`

---

### 2. Obtener Todas las Reservas

**Descripción**: Lista todas las reservas del sistema.

**Endpoint**: `GET /reservations`

**Respuesta exitosa** (200 OK):
```json
[
  {
    "id": 123,
    "userId": 10,
    "locationId": 1,
    "mealTime": "ALMUERZO",
    "reservationTimeSlot": "ALMUERZO_SLOT_1",
    "reservationDate": "2025-11-06T12:00:00",
    "status": "ACTIVA"
  },
  {
    "id": 124,
    "userId": 11,
    "mealTime": "CENA",
    "status": "CANCELADA"
  }
]
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/reservations`

---

### 3. Obtener Mis Reservas

**Descripción**: Lista las reservas de un usuario específico.

**Endpoint**: `GET /reservations/mine?userId={userId}`

**Query Params**:
- `userId` (Long): ID del usuario

**Ejemplo**: `GET /reservations/mine?userId=10`

**Respuesta exitosa** (200 OK):
```json
[
  {
    "id": 123,
    "userId": 10,
    "mealTime": "ALMUERZO",
    "reservationDate": "2025-11-06T12:00:00",
    "status": "ACTIVA"
  }
]
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/reservations/mine?userId=10`

---

### 4. Obtener Reserva por ID

**Descripción**: Obtiene los detalles de una reserva específica.

**Endpoint**: `GET /reservations/byreservationId/{reservationId}`

**Path Params**:
- `reservationId` (Long): ID de la reserva

**Ejemplo**: `GET /reservations/byreservationId/123`

**Respuesta exitosa** (200 OK):
```json
{
  "id": 123,
  "userId": 10,
  "locationId": 1,
  "mealTime": "ALMUERZO",
  "reservationTimeSlot": "ALMUERZO_SLOT_1",
  "reservationDate": "2025-11-06T12:00:00",
  "status": "ACTIVA"
}
```

**Respuesta error** (404 Not Found): Si la reserva no existe

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/reservations/byreservationId/123`

---

### 5. Cancelar Reserva

**Descripción**: Cancela una reserva existente.

**Endpoint**: `DELETE /reservations/{reservationId}`

**Path Params**:
- `reservationId` (Long): ID de la reserva a cancelar

**Ejemplo**: `DELETE /reservations/123`

**Respuesta exitosa** (200 OK):
```json
{
  "id": 123,
  "userId": 10,
  "status": "CANCELADA",
  "cancelledAt": "2025-11-01T22:00:00"
}
```

**Respuesta error** (404 Not Found): Si la reserva no existe

**Postman**:
- Method: `DELETE`
- URL: `http://localhost:4002/reservations/123`

**Evento RabbitMQ**: Publica evento `reservation.updated` con action `cancelled`

---

## 🛍️ Products

### 1. Listar Todos los Productos

**Descripción**: Obtiene la lista completa de productos disponibles.

**Endpoint**: `GET /products`

**Respuesta exitosa** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Pastel de papa",
    "price": 10000,
    "productType": "PLATO",
    "image": "https://example.com/image.jpg",
    "available": true
  },
  {
    "id": 2,
    "name": "Coca Cola",
    "price": 5000,
    "productType": "BEBIDA",
    "available": true
  }
]
```

**Tipos de producto**:
- `PLATO`: Plato principal
- `BEBIDA`: Bebidas
- `POSTRE`: Postres
- `ENTRADA`: Entradas

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/products`

---

### 2. Obtener Producto por ID

**Descripción**: Obtiene los detalles de un producto específico.

**Endpoint**: `GET /products/{id}`

**Path Params**:
- `id` (Long): ID del producto

**Ejemplo**: `GET /products/1`

**Respuesta exitosa** (200 OK):
```json
{
  "id": 1,
  "name": "Pastel de papa",
  "price": 10000,
  "productType": "PLATO",
  "image": "https://example.com/image.jpg",
  "available": true
}
```

**Respuesta error** (404 Not Found): Si el producto no existe

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/products/1`

---

### 3. Crear Producto

**Descripción**: Crea un nuevo producto en el sistema.

**Endpoint**: `POST /products`

**Body**:
```json
{
  "name": "Milanesa con papas",
  "price": 12000,
  "productType": "PLATO",
  "image": "https://example.com/milanesa.jpg",
  "available": true
}
```

**Campos**:
- `name` (String): Nombre del producto
- `price` (Double): Precio en pesos (sin decimales para simplificar)
- `productType` (String): Tipo - `"PLATO"`, `"BEBIDA"`, `"POSTRE"`, `"ENTRADA"`
- `image` (String): URL de la imagen
- `available` (Boolean): Si está disponible o no

**Respuesta exitosa** (201 Created):
```json
{
  "id": 15,
  "name": "Milanesa con papas",
  "price": 12000,
  "productType": "PLATO",
  "image": "https://example.com/milanesa.jpg",
  "available": true
}
```

**Postman**:
- Method: `POST`
- URL: `http://localhost:4002/products`
- Headers: `Content-Type: application/json`
- Body → raw → JSON

---

### 4. Actualizar Producto

**Descripción**: Actualiza parcialmente un producto existente.

**Endpoint**: `PATCH /products/{id}`

**Path Params**:
- `id` (Long): ID del producto a actualizar

**Body** (solo incluir campos a actualizar):
```json
{
  "price": 13000,
  "available": false
}
```

**Respuesta exitosa** (200 OK):
```json
{
  "id": 1,
  "name": "Pastel de papa",
  "price": 13000,
  "productType": "PLATO",
  "available": false
}
```

**Respuesta error** (404 Not Found): Si el producto no existe

**Postman**:
- Method: `PATCH`
- URL: `http://localhost:4002/products/1`
- Headers: `Content-Type: application/json`
- Body → raw → JSON

---

### 5. Eliminar Producto

**Descripción**: Elimina un producto del sistema.

**Endpoint**: `DELETE /products/{id}`

**Path Params**:
- `id` (Long): ID del producto a eliminar

**Ejemplo**: `DELETE /products/15`

**Respuesta exitosa** (204 No Content): Sin contenido, eliminado exitosamente

**Respuesta error** (404 Not Found): Si el producto no existe

**Postman**:
- Method: `DELETE`
- URL: `http://localhost:4002/products/15`

---

## 📅 Menus

### 1. Obtener Menú Completo

**Descripción**: Obtiene el menú completo de toda la semana.

**Endpoint**: `GET /menus`

**Respuesta exitosa** (200 OK):
```json
{
  "menuId": 1,
  "days": [
    {
      "day": "LUNES",
      "meals": [
        {
          "mealTime": "DESAYUNO",
          "products": [
            {
              "id": 1,
              "name": "Café con leche",
              "price": 2000,
              "productType": "BEBIDA"
            },
            {
              "id": 2,
              "name": "Medialunas",
              "price": 3000,
              "productType": "ENTRADA"
            }
          ]
        },
        {
          "mealTime": "ALMUERZO",
          "products": [...]
        }
      ]
    },
    {
      "day": "MARTES",
      "meals": [...]
    }
  ]
}
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/menus`

---

### 2. Obtener Menú Actual

**Descripción**: Obtiene el menú del día actual según el horario.

**Endpoint**: `GET /menus/now`

**Respuesta exitosa** (200 OK):
```json
{
  "day": "VIERNES",
  "currentMealTime": "ALMUERZO",
  "meals": [
    {
      "mealTime": "ALMUERZO",
      "products": [
        {
          "id": 5,
          "name": "Pastel de papa",
          "price": 10000,
          "productType": "PLATO"
        }
      ]
    }
  ]
}
```

**Lógica**: Determina el día actual y qué comida corresponde según la hora.

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/menus/now`

---

### 3. Obtener Horarios de Comida

**Descripción**: Obtiene los horarios y slots disponibles para cada comida.

**Endpoint**: `GET /menus/shift`

**Respuesta exitosa** (200 OK):
```json
[
  {
    "mealTime": "DESAYUNO",
    "timeRange": "07:00-09:00"
  },
  {
    "mealTime": "ALMUERZO",
    "timeRange": "12:00-14:00"
  },
  {
    "mealTime": "MERIENDA",
    "timeRange": "16:00-18:00"
  },
  {
    "mealTime": "CENA",
    "timeRange": "19:00-21:00"
  }
]
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/menus/shift`

---

### 4. Obtener Menú por Día

**Descripción**: Obtiene el menú de un día específico de la semana.

**Endpoint**: `GET /menus/{day}`

**Path Params**:
- `day` (String): Día de la semana - `LUNES`, `MARTES`, `MIERCOLES`, `JUEVES`, `VIERNES`, `SABADO`, `DOMINGO`

**Ejemplo**: `GET /menus/LUNES`

**Respuesta exitosa** (200 OK):
```json
{
  "day": "LUNES",
  "meals": [
    {
      "mealTime": "DESAYUNO",
      "products": [...]
    },
    {
      "mealTime": "ALMUERZO",
      "products": [...]
    },
    {
      "mealTime": "MERIENDA",
      "products": [...]
    },
    {
      "mealTime": "CENA",
      "products": [...]
    }
  ]
}
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/menus/LUNES`

---

### 5. Obtener Menú por Día y Comida

**Descripción**: Obtiene el menú de una comida específica de un día específico.

**Endpoint**: `GET /menus/{day}/{mealTime}`

**Path Params**:
- `day` (String): Día de la semana
- `mealTime` (String): Tipo de comida - `DESAYUNO`, `ALMUERZO`, `MERIENDA`, `CENA`

**Ejemplo**: `GET /menus/LUNES/ALMUERZO`

**Respuesta exitosa** (200 OK):
```json
{
  "day": "LUNES",
  "mealTime": "ALMUERZO",
  "products": [
    {
      "id": 5,
      "name": "Pastel de papa",
      "price": 10000,
      "productType": "PLATO"
    },
    {
      "id": 8,
      "name": "Ensalada mixta",
      "price": 6000,
      "productType": "ENTRADA"
    },
    {
      "id": 12,
      "name": "Agua mineral",
      "price": 3000,
      "productType": "BEBIDA"
    }
  ]
}
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/menus/LUNES/ALMUERZO`

---

### 6. Crear/Actualizar Menú

**Descripción**: Crea o actualiza el menú semanal completo.

**Endpoint**: `POST /menus`

**Body**:
```json
{
  "days": [
    {
      "day": "LUNES",
      "meals": [
        {
          "mealTime": "DESAYUNO",
          "productIds": [1, 2, 3]
        },
        {
          "mealTime": "ALMUERZO",
          "productIds": [5, 8, 12]
        }
      ]
    },
    {
      "day": "MARTES",
      "meals": [...]
    }
  ]
}
```

**Campos**:
- `days[]`: Array de días
  - `day`: Día de la semana
  - `meals[]`: Array de comidas del día
    - `mealTime`: Tipo de comida
    - `productIds[]`: Array de IDs de productos disponibles

**Respuesta exitosa** (201 Created):
```json
{
  "menuId": 1,
  "message": "Menú creado exitosamente",
  "days": [...]
}
```

**Postman**:
- Method: `POST`
- URL: `http://localhost:4002/menus`
- Headers: `Content-Type: application/json`
- Body → raw → JSON

---

### 7. Actualizar Menú de un Día

**Descripción**: Actualiza las comidas de un día específico.

**Endpoint**: `PATCH /menus/{day}`

**Path Params**:
- `day` (String): Día de la semana a actualizar

**Body**:
```json
{
  "meals": [
    {
      "mealTime": "ALMUERZO",
      "productIds": [5, 8, 12, 15]
    },
    {
      "mealTime": "CENA",
      "productIds": [7, 9, 11]
    }
  ]
}
```

**Respuesta exitosa** (200 OK):
```json
{
  "day": "LUNES",
  "meals": [...]
}
```

**Postman**:
- Method: `PATCH`
- URL: `http://localhost:4002/menus/LUNES`
- Headers: `Content-Type: application/json`
- Body → raw → JSON

---

## 📍 Locations

### 1. Listar Todas las Ubicaciones

**Descripción**: Obtiene todas las ubicaciones/comedores disponibles.

**Endpoint**: `GET /locations`

**Respuesta exitosa** (200 OK):
```json
[
  {
    "id": 1,
    "name": "Norte",
    "capacity": 100,
    "address": "Av. Libertador 123"
  },
  {
    "id": 2,
    "name": "Centro",
    "capacity": 150,
    "address": "Av. Corrientes 456"
  },
  {
    "id": 3,
    "name": "Sur",
    "capacity": 80,
    "address": "Av. Rivadavia 789"
  }
]
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/locations`

---

### 2. Obtener Ubicación por ID

**Descripción**: Obtiene los detalles de una ubicación específica.

**Endpoint**: `GET /locations/{id}`

**Path Params**:
- `id` (Long): ID de la ubicación

**Ejemplo**: `GET /locations/1`

**Respuesta exitosa** (200 OK):
```json
{
  "id": 1,
  "name": "Norte",
  "capacity": 100,
  "address": "Av. Libertador 123"
}
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/locations/1`

---

### 3. Ver Disponibilidad de Slots

**Descripción**: Consulta la disponibilidad de slots de tiempo para una ubicación, comida y fecha específica.

**Endpoint**: `GET /locations/{id}/availability`

**Path Params**:
- `id` (Long): ID de la ubicación

**Query Params**:
- `mealTime` (String): Tipo de comida - `DESAYUNO`, `ALMUERZO`, `MERIENDA`, `CENA`
- `date` (DateTime): Fecha en formato ISO `2025-11-06T00:00:00`

**Ejemplo**: `GET /locations/1/availability?mealTime=ALMUERZO&date=2025-11-06T00:00:00`

**Respuesta exitosa** (200 OK):
```json
[
  {
    "slot": "ALMUERZO_SLOT_1",
    "timeRange": "12:00-12:30",
    "available": true,
    "reservedCount": 15,
    "capacity": 50
  },
  {
    "slot": "ALMUERZO_SLOT_2",
    "timeRange": "12:30-13:00",
    "available": true,
    "reservedCount": 32,
    "capacity": 50
  },
  {
    "slot": "ALMUERZO_SLOT_3",
    "timeRange": "13:00-13:30",
    "available": false,
    "reservedCount": 50,
    "capacity": 50
  },
  {
    "slot": "ALMUERZO_SLOT_4",
    "timeRange": "13:30-14:00",
    "available": true,
    "reservedCount": 8,
    "capacity": 50
  }
]
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/locations/1/availability?mealTime=ALMUERZO&date=2025-11-06T00:00:00`

**Nota**: Los slots se generan dinámicamente según la configuración de horarios en `/menus/schedules`

---

## 🛒 Carts

### 1. Crear Carrito

**Descripción**: Crea un nuevo carrito de compras para un usuario.

**Endpoint**: `POST /carts`

**Body**:
```json
{
  "userId": 10
}
```

**Respuesta exitosa** (201 Created):
```json
{
  "id": 1,
  "userId": 10,
  "items": [],
  "total": 0,
  "status": "ACTIVE",
  "createdAt": "2025-11-01T21:00:00"
}
```

**Postman**:
- Method: `POST`
- URL: `http://localhost:4002/carts`
- Headers: `Content-Type: application/json`
- Body → raw → JSON

---

### 2. Actualizar Carrito

**Descripción**: Actualiza el contenido de un carrito (agrega/quita productos).

**Endpoint**: `PUT /carts/{id}`

**Path Params**:
- `id` (Long): ID del carrito

**Body**:
```json
{
  "userId": 10,
  "productIds": [1, 2, 5, 8]
}
```

**Respuesta exitosa** (200 OK):
```json
{
  "id": 1,
  "userId": 10,
  "items": [
    {
      "productId": 1,
      "productName": "Pastel de papa",
      "price": 10000,
      "quantity": 1
    },
    {
      "productId": 2,
      "productName": "Coca Cola",
      "price": 5000,
      "quantity": 1
    }
  ],
  "total": 15000
}
```

**Postman**:
- Method: `PUT`
- URL: `http://localhost:4002/carts/1`
- Headers: `Content-Type: application/json`
- Body → raw → JSON

---

### 3. Eliminar Carrito

**Descripción**: Elimina un carrito de compras.

**Endpoint**: `DELETE /carts/{id}`

**Path Params**:
- `id` (Long): ID del carrito a eliminar

**Ejemplo**: `DELETE /carts/1`

**Respuesta exitosa** (204 No Content): Sin contenido, eliminado exitosamente

**Postman**:
- Method: `DELETE`
- URL: `http://localhost:4002/carts/1`

---

### 4. Confirmar Carrito (Genera Factura)

**Descripción**: Confirma el carrito y genera una factura. Este proceso convierte el carrito en una compra real.

**Endpoint**: `POST /carts/confirmation/{id}`

**Path Params**:
- `id` (Long): ID del carrito a confirmar

**Ejemplo**: `POST /carts/confirmation/1`

**Respuesta exitosa** (201 Created):
```json
{
  "id": 1,
  "date": "2025-11-01T21:30:00",
  "subtotal": 15000,
  "reservationId": null,
  "items": [
    {
      "productId": 1,
      "productName": "Pastel de papa",
      "price": 10000,
      "quantity": 1
    },
    {
      "productId": 2,
      "productName": "Coca Cola",
      "price": 5000,
      "quantity": 1
    }
  ]
}
```

**Postman**:
- Method: `POST`
- URL: `http://localhost:4002/carts/confirmation/1`

**Evento RabbitMQ**: Publica evento `bill.created`

**Nota**: Después de confirmar, el carrito se elimina automáticamente.

---

## 💰 Bills

### 1. Listar Facturas

**Descripción**: Obtiene las facturas del sistema, opcionalmente filtradas por rango de fechas.

**Endpoint**: `GET /bills`

**Query Params** (opcionales):
- `startDate` (DateTime): Fecha de inicio en formato ISO `2025-11-01T00:00:00`
- `endDate` (DateTime): Fecha de fin en formato ISO `2025-11-30T23:59:59`

**Ejemplos**:
- Todas las facturas: `GET /bills`
- Por rango: `GET /bills?startDate=2025-11-01T00:00:00&endDate=2025-11-30T23:59:59`

**Respuesta exitosa** (200 OK):
```json
[
  {
    "id": 1,
    "date": "2025-11-01T21:30:00",
    "subtotal": 15000,
    "reservationId": null,
    "items": [
      {
        "productId": 1,
        "productName": "Pastel de papa",
        "price": 10000,
        "quantity": 1
      },
      {
        "productId": 2,
        "productName": "Coca Cola",
        "price": 5000,
        "quantity": 1
      }
    ]
  },
  {
    "id": 2,
    "date": "2025-11-02T12:15:00",
    "subtotal": 22000,
    "reservationId": 123,
    "items": [...]
  }
]
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/bills`
- O con filtros: `http://localhost:4002/bills?startDate=2025-11-01T00:00:00&endDate=2025-11-30T23:59:59`

---

## ⏰ Meal Schedules

### 1. Obtener Todos los Horarios

**Descripción**: Lista los horarios configurados para todas las comidas.

**Endpoint**: `GET /menus/schedules`

**Respuesta exitosa** (200 OK):
```json
[
  {
    "mealTime": "DESAYUNO",
    "startTime": "07:00",
    "endTime": "09:00",
    "slotDurationMinutes": 30,
    "totalSlots": 4
  },
  {
    "mealTime": "ALMUERZO",
    "startTime": "12:00",
    "endTime": "14:00",
    "slotDurationMinutes": 30,
    "totalSlots": 4
  },
  {
    "mealTime": "MERIENDA",
    "startTime": "16:00",
    "endTime": "18:00",
    "slotDurationMinutes": 30,
    "totalSlots": 4
  },
  {
    "mealTime": "CENA",
    "startTime": "19:00",
    "endTime": "21:00",
    "slotDurationMinutes": 30,
    "totalSlots": 4
  }
]
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/menus/schedules`

---

### 2. Obtener Horario de una Comida

**Descripción**: Obtiene el horario configurado para una comida específica.

**Endpoint**: `GET /menus/schedules/{mealTime}`

**Path Params**:
- `mealTime` (String): Tipo de comida - `DESAYUNO`, `ALMUERZO`, `MERIENDA`, `CENA`

**Ejemplo**: `GET /menus/schedules/ALMUERZO`

**Respuesta exitosa** (200 OK):
```json
{
  "mealTime": "ALMUERZO",
  "startTime": "12:00",
  "endTime": "14:00",
  "slotDurationMinutes": 30,
  "totalSlots": 4
}
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/menus/schedules/ALMUERZO`

---

### 3. Actualizar Horario de una Comida

**Descripción**: Actualiza la configuración de horarios para una comida específica.

**Endpoint**: `PUT /menus/schedules/{mealTime}`

**Path Params**:
- `mealTime` (String): Tipo de comida a actualizar

**Query Params**:
- `startTime` (Time): Hora de inicio en formato `HH:mm` (ej: `12:00`)
- `endTime` (Time): Hora de fin en formato `HH:mm` (ej: `14:00`)
- `slotDurationMinutes` (Integer): Duración de cada slot en minutos (ej: `30`)

**Ejemplo**: `PUT /menus/schedules/ALMUERZO?startTime=12:00&endTime=14:30&slotDurationMinutes=30`

**Respuesta exitosa** (200 OK):
```json
{
  "mealTime": "ALMUERZO",
  "startTime": "12:00",
  "endTime": "14:30",
  "slotDurationMinutes": 30,
  "totalSlots": 5
}
```

**Postman**:
- Method: `PUT`
- URL: `http://localhost:4002/menus/schedules/ALMUERZO?startTime=12:00&endTime=14:30&slotDurationMinutes=30`

**Nota**: Esto afecta dinámicamente los slots disponibles en `/locations/{id}/availability`

---

## 🔐 Sessions

### 1. Test Endpoint

**Descripción**: Endpoint de prueba para verificar que el sistema está funcionando.

**Endpoint**: `GET /sessions/test`

**Respuesta exitosa** (200 OK):
```json
"Test endpoint is working!"
```

**Postman**:
- Method: `GET`
- URL: `http://localhost:4002/sessions/test`

**Nota**: Este es un endpoint básico. La autenticación completa con JWT está pendiente.

---

## 🎯 Flujos Completos en Postman

### Flujo 1: Usuario Hace una Reserva

1. **Ver ubicaciones disponibles**
   - `GET /locations`
   
2. **Ver disponibilidad de slots**
   - `GET /locations/1/availability?mealTime=ALMUERZO&date=2025-11-06T00:00:00`
   
3. **Ver el menú del día**
   - `GET /menus/MIERCOLES/ALMUERZO`
   
4. **Crear la reserva**
   - `POST /reservations` con el slot elegido
   
5. **Verificar mis reservas**
   - `GET /reservations/mine?userId=10`

### Flujo 2: Usuario Compra Productos

1. **Ver productos disponibles**
   - `GET /products`
   
2. **Crear carrito**
   - `POST /carts` con userId
   
3. **Agregar productos al carrito**
   - `PUT /carts/{cartId}` con array de productIds
   
4. **Confirmar carrito (genera factura)**
   - `POST /carts/confirmation/{cartId}`
   
5. **Ver mi factura**
   - `GET /bills`

### Flujo 3: Admin Gestiona el Menú

1. **Ver menú actual**
   - `GET /menus`
   
2. **Actualizar menú de un día**
   - `PATCH /menus/LUNES` con nuevos productos
   
3. **Ver horarios actuales**
   - `GET /menus/schedules`
   
4. **Actualizar horario de almuerzo**
   - `PUT /menus/schedules/ALMUERZO` con nuevos tiempos
   
5. **Verificar cambios**
   - `GET /menus/now`

---

## 📝 Notas Importantes

### Formato de Fechas
Todos los campos de tipo `DateTime` usan formato ISO 8601:
```
2025-11-06T12:00:00
```

### Tipos de Comida (MealTime)
- `DESAYUNO`
- `ALMUERZO`
- `MERIENDA`
- `CENA`

### Días de la Semana (DayOfWeek)
- `LUNES`
- `MARTES`
- `MIERCOLES`
- `JUEVES`
- `VIERNES`
- `SABADO`
- `DOMINGO`

### Tipos de Producto (ProductType)
- `PLATO`
- `BEBIDA`
- `POSTRE`
- `ENTRADA`

### Estados de Reserva
- `ACTIVA`: Reserva confirmada
- `CANCELADA`: Reserva cancelada
- `COMPLETADA`: Usuario asistió

### Slots de Tiempo
Los slots se generan dinámicamente según la configuración en `/menus/schedules`.
Ejemplo: Si ALMUERZO es de 12:00 a 14:00 con slots de 30 minutos:
- `ALMUERZO_SLOT_1`: 12:00-12:30
- `ALMUERZO_SLOT_2`: 12:30-13:00
- `ALMUERZO_SLOT_3`: 13:00-13:30
- `ALMUERZO_SLOT_4`: 13:30-14:00

---

## 🔔 Eventos RabbitMQ

Los siguientes endpoints publican eventos a RabbitMQ:

### `POST /reservations` → `reservation.updated` (action: created)
```json
{
  "eventType": "reservation.updated",
  "payload": {
    "reservationId": "123",
    "action": "created",
    "startDateTime": "2025-11-06T12:00:00",
    "endDateTime": "2025-11-06T13:00:00"
  }
}
```

### `DELETE /reservations/{id}` → `reservation.updated` (action: cancelled)
```json
{
  "eventType": "reservation.updated",
  "payload": {
    "reservationId": "123",
    "action": "cancelled"
  }
}
```

### `POST /carts/confirmation/{id}` → `bill.created`
```json
{
  "eventType": "bill.created",
  "payload": {
    "id": "bill-123",
    "subtotal": 15000,
    "products": [...]
  }
}
```

Ver `QUICK_START_RABBITMQ.md` para más detalles sobre eventos.

---

## ✅ Testing Checklist

- [ ] Puedo crear una reserva
- [ ] Puedo ver mis reservas
- [ ] Puedo cancelar una reserva
- [ ] Puedo ver todos los productos
- [ ] Puedo crear un producto
- [ ] Puedo actualizar un producto
- [ ] Puedo ver el menú completo
- [ ] Puedo ver el menú actual
- [ ] Puedo actualizar el menú de un día
- [ ] Puedo ver las ubicaciones
- [ ] Puedo ver la disponibilidad de slots
- [ ] Puedo crear un carrito
- [ ] Puedo agregar productos al carrito
- [ ] Puedo confirmar el carrito (genera factura)
- [ ] Puedo ver las facturas
- [ ] Puedo ver los horarios de comida
- [ ] Puedo actualizar horarios de comida

---

¡Listo! 🎉 Ahora tienes la documentación completa de la API para probar en Postman.
